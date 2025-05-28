package dev.thomasglasser.mineraculous.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.ServerLookData;
import dev.thomasglasser.tommylib.api.packs.PackInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class MineraculousCoreEvents {
    // Registration
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(MineraculousBuiltInRegistries.ABILITY_SERIALIZER);
    }

    public static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MineraculousRegistries.MIRACULOUS, Miraculous.CODEC, Miraculous.CODEC);
        event.dataPackRegistry(MineraculousRegistries.ABILITY, Ability.DIRECT_CODEC, Ability.DIRECT_CODEC);
        event.dataPackRegistry(MineraculousRegistries.KAMIKOTIZATION, Kamikotization.CODEC, Kamikotization.CODEC);
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        for (PackInfo info : MineraculousPacks.getPacks()) {
            if (event.getPackType() == info.type()) {
                var resourcePath = ModList.get().getModFileById(Mineraculous.MOD_ID).getFile().findResource("packs/" + info.knownPack().namespace() + "/" + info.knownPack().id());
                var pack = Pack.readMetaAndCreate(new PackLocationInfo("builtin/" + info.knownPack().id(), Component.translatable(info.titleKey()), info.source(), Optional.of(info.knownPack())), new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openFull(PackLocationInfo p_326241_, Pack.Metadata p_325959_) {
                        return new PathPackResources(p_326241_, resourcePath);
                    }

                    @Override
                    public PackResources openPrimary(PackLocationInfo p_326301_) {
                        return new PathPackResources(p_326301_, resourcePath);
                    }
                }, info.type(), PackInfo.BUILT_IN_SELECTION_CONFIG);
                event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
            }
        }
    }

    public static void onRegisterDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS);
        event.register(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS);
        event.register(MineraculousDataMaps.ENTITY_LUCKY_CHARMS);

        event.register(MineraculousDataMaps.MIRACULOUS_EFFECTS);
        event.register(MineraculousDataMaps.MIRACULOUS_ATTRIBUTE_MODIFIERS);

        event.register(MineraculousDataMaps.AGEABLES);
    }

    // Look Loading
    public static void onServerStarted(ServerStartedEvent event) {
        Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> miraculousLooks = new HashMap<>();
        Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> suitLooks = new HashMap<>();
        Registry<Miraculous> registry = event.getServer().registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS);
        registry.holders().forEach(miraculous -> {
            suitLooks.put(miraculous.key(), fetchSuitLooks(event.getServer().getServerDirectory(), miraculous));
            miraculousLooks.put(miraculous.key(), fetchMiraculousLooks(event.getServer().getServerDirectory(), miraculous.key()));
        });
        List<Either<UUID, String>> whitelist = new ArrayList<>();
        Gson gson = new Gson();
        Path whitelistFile = event.getServer().getServerDirectory().resolve("miraculouslooks").resolve("whitelist.json");
        if (Files.exists(whitelistFile)) {
            try (BufferedReader reader = Files.newBufferedReader(whitelistFile)) {
                JsonArray jsonObject = gson.fromJson(reader, JsonArray.class);
                for (JsonElement entry : jsonObject.asList()) {
                    String id = entry.getAsString();
                    try {
                        UUID uuid = UUID.fromString(id);
                        whitelist.add(Either.left(uuid));
                    } catch (IllegalArgumentException e) {
                        whitelist.add(Either.right(id));
                    }
                }
            } catch (IOException e) {
                Mineraculous.LOGGER.error("Failed to read whitelist file", e);
            }
        }
        List<Either<UUID, String>> blacklist = new ArrayList<>();
        Path blacklistFile = event.getServer().getServerDirectory().resolve("miraculouslooks").resolve("blacklist.json");
        if (Files.exists(blacklistFile)) {
            try (BufferedReader reader = Files.newBufferedReader(blacklistFile)) {
                JsonArray jsonObject = gson.fromJson(reader, JsonArray.class);
                for (JsonElement entry : jsonObject.asList()) {
                    String id = entry.getAsString();
                    try {
                        UUID uuid = UUID.fromString(id);
                        blacklist.add(Either.left(uuid));
                    } catch (IllegalArgumentException e) {
                        blacklist.add(Either.right(id));
                    }
                }
            } catch (IOException e) {
                Mineraculous.LOGGER.error("Failed to read blacklist file", e);
            }
        }
        ServerLookData.set(suitLooks, miraculousLooks, whitelist, blacklist);
        if (event.getServer().registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).size() == 0) {
            Mineraculous.LOGGER.warn(Component.translatable(Kamikotization.NO_KAMIKOTIZATIONS).getString());
        }
    }

    public static Map<String, FlattenedSuitLookData> fetchSuitLooks(Path root, Holder<Miraculous> miraculous) {
        int transformationFrames = miraculous.value().transformationFrames();
        String namespace = miraculous.getKey().location().getNamespace();
        String type = miraculous.getKey().location().getPath();
        root = root.resolve("miraculouslooks");
        if (!Files.exists(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException e) {
                Mineraculous.LOGGER.error("Failed to create miraculous look directory", e);
            }
        }
        Map<String, FlattenedSuitLookData> suitLooks = fetchSuitLooks(root.resolve("suits").resolve(namespace).resolve(type), transformationFrames, "");
        if (Files.exists(root) && Files.isDirectory(root)) {
            try (Stream<Path> files = Files.list(root)) {
                for (Path file : files.toList()) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".zip")) {
                        String zipNamespace = fileName.toLowerCase().replace(".zip", "").replace(" ", "_");
                        try (FileSystem fs = FileSystems.newFileSystem(file)) {
                            suitLooks.putAll(fetchSuitLooks(fs.getRootDirectories().iterator().next().resolve("suits").resolve(namespace).resolve(type), transformationFrames, zipNamespace));
                        }
                    }
                }
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle suit look syncing", exception);
            }
        }
        return suitLooks;
    }

    public static Map<String, FlattenedSuitLookData> fetchSuitLooks(Path folder, int transformationFrames, String namespace) {
        Map<String, FlattenedSuitLookData> suitLooks = new HashMap<>();
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            try (Stream<Path> files = Files.list(folder)) {
                for (Path texture : files.toList()) {
                    String fileName = texture.getFileName().toString();
                    if (fileName.endsWith(".png") && fileName.chars().noneMatch(Character::isDigit) && !fileName.contains("glowmask")) {
                        String look = fileName.replace(".png", "");
                        try {
                            Path model = texture.resolveSibling(look + ".geo.json");
                            String convertedModel = null;
                            if (Files.exists(model)) {
                                convertedModel = Files.readString(model);
                            }
                            byte[] convertedImage = NativeImage.read(texture.toUri().toURL().openStream()).asByteArray();
                            Path glowmask = texture.resolveSibling(look + "_glowmask.png");
                            byte[] convertedGlowmask = null;
                            if (Files.exists(glowmask)) {
                                convertedGlowmask = NativeImage.read(glowmask.toUri().toURL().openStream()).asByteArray();
                            }
                            List<byte[]> convertedFrames = new ArrayList<>();
                            List<byte[]> convertedGlowmaskFrames = new ArrayList<>();
                            for (int i = 1; i <= transformationFrames; i++) {
                                Path frame = texture.resolveSibling(look + "_" + i + ".png");
                                if (Files.exists(frame)) {
                                    convertedFrames.add(NativeImage.read(frame.toUri().toURL().openStream()).asByteArray());
                                }
                                Path glowmaskFrame = texture.resolveSibling(look + "_" + i + "_glowmask.png");
                                if (Files.exists(glowmaskFrame)) {
                                    convertedGlowmaskFrames.add(NativeImage.read(glowmaskFrame.toUri().toURL().openStream()).asByteArray());
                                }
                            }
                            String convertedAnimations = null;
                            Path animations = texture.resolveSibling(look + ".animation.json");
                            if (Files.exists(animations)) {
                                convertedAnimations = Files.readString(animations);
                            }
                            String lookName = namespace.isEmpty() ? look : namespace + "/" + look;
                            suitLooks.put(lookName, new FlattenedSuitLookData(lookName, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), convertedFrames, convertedGlowmaskFrames, Optional.ofNullable(convertedAnimations)));
                        } catch (Exception exception) {
                            Mineraculous.LOGGER.error("Failed to handle suit look syncing", exception);
                        }
                    }
                }
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle suit look syncing", exception);
            }
        }
        return suitLooks;
    }

    public static Map<String, FlattenedMiraculousLookData> fetchMiraculousLooks(Path root, ResourceKey<Miraculous> miraculousKey) {
        String namespace = miraculousKey.location().getNamespace();
        String type = miraculousKey.location().getPath();
        root = root.resolve("miraculouslooks");
        if (!Files.exists(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException e) {
                Mineraculous.LOGGER.error("Failed to create miraculous look directory", e);
            }
        }
        Map<String, FlattenedMiraculousLookData> miraculousLooks = fetchMiraculousLooks(root.resolve("miraculouses").resolve(namespace).resolve(type), "");
        if (Files.exists(root) && Files.isDirectory(root)) {
            try (Stream<Path> files = Files.list(root)) {
                for (Path file : files.toList()) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".zip")) {
                        String zipNamespace = fileName.toLowerCase().replace(".zip", "").replace(" ", "_");
                        try (FileSystem fs = FileSystems.newFileSystem(file)) {
                            miraculousLooks.putAll(fetchMiraculousLooks(fs.getRootDirectories().iterator().next().resolve("miraculous").resolve(namespace).resolve(type), zipNamespace));
                        }
                    }
                }
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle miraculous look syncing", exception);
            }
        }
        return miraculousLooks;
    }

    public static Map<String, FlattenedMiraculousLookData> fetchMiraculousLooks(Path folder, String namespace) {
        Map<String, FlattenedMiraculousLookData> miraculousLooks = new HashMap<>();
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            try (Stream<Path> files = Files.list(folder)) {
                for (Path texture : files.toList()) {
                    String fileName = texture.getFileName().toString();
                    if (fileName.endsWith(".png") && fileName.chars().noneMatch(Character::isDigit) && !fileName.contains("glowmask")) {
                        String look = fileName.replace(".png", "");
                        try {
                            Path model = texture.resolveSibling(look + ".geo.json");
                            String convertedModel = null;
                            if (Files.exists(model)) {
                                convertedModel = Files.readString(model);
                            }
                            byte[] convertedImage = NativeImage.read(texture.toUri().toURL().openStream()).asByteArray();
                            Path glowmask = texture.resolveSibling(look + "_glowmask.png");
                            byte[] convertedGlowmask = null;
                            if (Files.exists(glowmask)) {
                                convertedGlowmask = NativeImage.read(glowmask.toUri().toURL().openStream()).asByteArray();
                            }
                            Path transforms = texture.resolveSibling(look + ".json");
                            String convertedDisplay = null;
                            if (Files.exists(transforms)) {
                                convertedDisplay = Files.readString(transforms);
                            }
                            String lookName = namespace.isEmpty() ? look : namespace + "/" + look;
                            miraculousLooks.put(lookName, new FlattenedMiraculousLookData(lookName, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedDisplay)));
                        } catch (Exception exception) {
                            Mineraculous.LOGGER.error("Failed to handle miraculous look syncing", exception);
                        }
                    }
                }
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle miraculous look syncing", exception);
            }
        }
        return miraculousLooks;
    }

    // Misc
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(BuiltInLootTables.SNIFFER_DIGGING.location())) {
            LootPool main = event.getTable().getPool("main");
            if (main != null) {
                ArrayList<LootPoolEntryContainer> entries = new ArrayList<>(main.entries);
                entries.add(LootItem.lootTableItem(MineraculousBlocks.HIBISCUS_BUSH.asItem()).build());
                main.entries = entries;
            }
        }
    }
}
