package dev.thomasglasser.mineraculous.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

public class MineraculousCoreEvents {
    public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
        MineraculousPayloads.PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));
    }

    public static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MineraculousRegistries.MIRACULOUS, Miraculous.CODEC, Miraculous.CODEC);
        event.dataPackRegistry(MineraculousRegistries.ABILITY, Ability.DIRECT_CODEC, Ability.DIRECT_CODEC);
        event.dataPackRegistry(MineraculousRegistries.KAMIKOTIZATION, Kamikotization.CODEC, Kamikotization.CODEC);
    }

    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(MineraculousBuiltInRegistries.ABILITY_SERIALIZER);
    }

    public static void onLoadLootTable(LootTableLoadEvent event) {
        if (event.getName().equals(BuiltInLootTables.SNIFFER_DIGGING.location())) {
            LootPool main = event.getTable().getPool("main");
            if (main != null) {
                ArrayList<LootPoolEntryContainer> entries = new ArrayList<>(main.entries);
                entries.add(LootItem.lootTableItem(MineraculousBlocks.HIBISCUS_BUSH.asItem()).build());
                main.entries = entries;
            }
        }
    }

    public static void onServerStarted(ServerStartedEvent event) {
        Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> miraculousLooks = new HashMap<>();
        Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> suitLooks = new HashMap<>();
        Registry<Miraculous> registry = event.getServer().registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS);
        registry.holders().forEach(miraculous -> {
            String namespace = miraculous.key().location().getNamespace();
            String type = miraculous.key().location().getPath();
            File folder = new File(event.getServer().getServerDirectory().toFile(), "miraculouslooks" + File.separator + "suits" + File.separator + namespace + File.separator + type);
            Map<String, FlattenedSuitLookData> commonSuitLookData = new HashMap<>();
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File texture : files) {
                        if (texture.getName().endsWith(".png") && texture.getName().chars().noneMatch(Character::isDigit) && !texture.getName().contains("glowmask")) {
                            String look = texture.getName().replace(".png", "");
                            try {
                                File model = new File(folder, look + ".geo.json");
                                String convertedModel = null;
                                if (model.exists()) {
                                    convertedModel = Files.readString(model.toPath());
                                }
                                byte[] convertedImage = NativeImage.read(texture.toPath().toUri().toURL().openStream()).asByteArray();
                                File glowmask = new File(folder, look + "_glowmask.png");
                                byte[] convertedGlowmask = null;
                                if (glowmask.exists()) {
                                    convertedGlowmask = NativeImage.read(glowmask.toPath().toUri().toURL().openStream()).asByteArray();
                                }
                                List<byte[]> convertedFrames = new ArrayList<>();
                                List<byte[]> convertedGlowmaskFrames = new ArrayList<>();
                                for (int i = 1; i <= miraculous.value().transformationFrames(); i++) {
                                    File frame = new File(folder, look + "_" + i + ".png");
                                    if (frame.exists()) {
                                        convertedFrames.add(NativeImage.read(frame.toPath().toUri().toURL().openStream()).asByteArray());
                                    }
                                    File glowmaskFrame = new File(folder, look + "_" + i + "_glowmask.png");
                                    if (glowmaskFrame.exists()) {
                                        convertedGlowmaskFrames.add(NativeImage.read(glowmaskFrame.toPath().toUri().toURL().openStream()).asByteArray());
                                    }
                                }
                                commonSuitLookData.put(look, new FlattenedSuitLookData(miraculous.key(), look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), convertedFrames, convertedGlowmaskFrames));
                            } catch (Exception exception) {
                                Mineraculous.LOGGER.error("Failed to handle common suit look syncing", exception);
                            }
                        }
                    }
                }
            }
            folder = new File(event.getServer().getServerDirectory().toFile(), "miraculouslooks" + File.separator + "miraculous" + File.separator + namespace + File.separator + type);
            Map<String, FlattenedMiraculousLookData> commonMiraculousLookData = new HashMap<>();
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File texture : files) {
                        if (texture.getName().endsWith(".png")) {
                            String look = texture.getName().replace(".png", "");
                            try {
                                File model = new File(folder, look + ".geo.json");
                                String convertedModel = null;
                                if (model.exists()) {
                                    convertedModel = Files.readString(model.toPath());
                                }
                                byte[] convertedImage = NativeImage.read(texture.toPath().toUri().toURL().openStream()).asByteArray();
                                File glowmask = new File(folder, look + "_glowmask.png");
                                byte[] convertedGlowmask = null;
                                if (glowmask.exists()) {
                                    convertedGlowmask = NativeImage.read(glowmask.toPath().toUri().toURL().openStream()).asByteArray();
                                }
                                File transforms = new File(folder, look + ".json");
                                String convertedDisplay = null;
                                if (transforms.exists()) {
                                    convertedDisplay = Files.readString(transforms.toPath());
                                }
                                commonMiraculousLookData.put(look, new FlattenedMiraculousLookData(miraculous.key(), look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedDisplay)));
                            } catch (Exception exception) {
                                Mineraculous.LOGGER.error("Failed to handle common miraculous look syncing", exception);
                            }
                        }
                    }
                }
            }
            miraculousLooks.put(miraculous.key(), commonMiraculousLookData);
            suitLooks.put(miraculous.key(), commonSuitLookData);
        });
        List<Either<UUID, String>> whitelist = new ArrayList<>();
        Gson gson = new Gson();
        File whitelistFile = new File(event.getServer().getServerDirectory().toFile(), "miraculouslooks/whitelist.json");
        if (whitelistFile.exists()) {
            try {
                JsonArray jsonObject = gson.fromJson(new FileReader(whitelistFile), JsonArray.class);
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
        File blacklistFile = new File(event.getServer().getServerDirectory().toFile(), "miraculouslooks/blacklist.json");
        if (blacklistFile.exists()) {
            try {
                JsonArray jsonObject = gson.fromJson(new FileReader(blacklistFile), JsonArray.class);
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
        ((FlattenedLookDataHolder) event.getServer().overworld()).mineraculous$setCommonMiraculousLookData(miraculousLooks);
        ((FlattenedLookDataHolder) event.getServer().overworld()).mineraculous$setCommonSuitLookData(suitLooks);
        ((FlattenedLookDataHolder) event.getServer().overworld()).mineraculous$setWhitelist(whitelist);
        ((FlattenedLookDataHolder) event.getServer().overworld()).mineraculous$setBlacklist(blacklist);
    }
}
