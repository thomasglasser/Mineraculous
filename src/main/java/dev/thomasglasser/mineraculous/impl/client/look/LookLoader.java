package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import software.bernie.geckolib.loading.FileLoader;

public class LookLoader {
    public static final Path LOOKS_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve(ServerLookManager.LOOKS_SUBPATH);
    public static final Path CACHE_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve(ServerLookManager.CACHE_SUBPATH);

    public static final String JSON_NAME = "look.json";

    public static final String NAME_KEY = "name";
    public static final String AUTHOR_KEY = "author";
    public static final String VALID_MIRACULOUSES_KEY = "valid_miraculouses";
    public static final String ASSETS_KEY = "assets";

    public static CompletableFuture<Void> reloadDefaults(PreparableReloadListener.PreparationBarrier stage, ResourceManager resourceManager,
            ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor,
            Executor gameExecutor) {
        ImmutableMap.Builder<ResourceLocation, DefaultLook> map = new ImmutableMap.Builder<>();
        return loadDefaults(backgroundExecutor, resourceManager, map::put)
                .thenCompose(stage::wait)
                .thenAcceptAsync(empty -> InternalLookManager.setDefaults(map.build()), gameExecutor);
    }

    private static CompletableFuture<Void> loadDefaults(Executor executor, ResourceManager resourceManager, BiConsumer<ResourceLocation, DefaultLook> map) {
        return CompletableFuture.runAsync(() -> CompletableFuture.supplyAsync(
                () -> resourceManager.listResources(ServerLookManager.LOOKS_SUBPATH.toString().replace('\\', '/'), fileName -> fileName.toString().endsWith(".json")), executor)
                .thenApplyAsync(resources -> {
                    Map<ResourceLocation, CompletableFuture<DefaultLook>> tasks = new Object2ObjectOpenHashMap<>();

                    for (ResourceLocation resource : resources.keySet()) {
                        tasks.put(resource, CompletableFuture.supplyAsync(() -> DefaultLook.load(FileLoader.loadFile(resource, resourceManager), resource), executor));
                    }

                    return tasks;
                }, executor)
                .thenAcceptAsync(tasks -> {
                    for (Map.Entry<ResourceLocation, CompletableFuture<DefaultLook>> entry : tasks.entrySet()) {
                        map.accept(entry.getKey(), entry.getValue().join());
                    }
                }, executor));
    }

    public static void load() {
        InternalLookManager.refresh();

        CompletableFuture.runAsync(() -> {
            try {
                ServerLookManager.clearOrCreateCache(CACHE_PATH);

                try (Stream<Path> paths = Files.list(LOOKS_PATH)) {
                    paths.forEach(path -> {
                        if (path.equals(CACHE_PATH))
                            return;
                        load(path, !path.startsWith(CACHE_PATH));
                    });
                }
            } catch (Exception e) {
                InternalLookManager.enterSafeMode(e.getMessage());
            }
        });
    }

    public static void load(Path path, boolean equippable) {
        if (InternalLookManager.isInSafeMode())
            return;
        try {
            String hash;
            if (Files.isDirectory(path)) {
                try {
                    byte[] zipBytes = ServerLookManager.zipFolderToBytes(path);
                    hash = Hashing.sha256().hashBytes(zipBytes).toString();
                    ServerLookManager.ensureCacheExists(CACHE_PATH);
                    Path look = CACHE_PATH.resolve(hash + ".look");
                    Files.write(look, zipBytes);
                    load(look, equippable);
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.error("Failed to parse look folder {}: {}", path, e.getMessage());
                }
            } else if (path.toString().endsWith(".zip") || path.toString().endsWith(".look")) {
                try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                    loadFromRoot(fs.getPath("/"), path, com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha256()).toString(), equippable);
                }
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load look from {}: {}", path, e.getMessage());
        }
    }

    private static void loadFromRoot(Path root, Path source, String hash, boolean equippable) throws Exception {
        if (InternalLookManager.isInSafeMode())
            return;

        Path path = root.resolve(JSON_NAME);
        if (!Files.exists(path))
            throw new FileNotFoundException("Missing " + JSON_NAME + " in " + root);

        JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        if (!json.has(NAME_KEY)) throw new IOException("Look missing " + NAME_KEY);
        String displayName = json.get(NAME_KEY).getAsString();

        String author = json.has(AUTHOR_KEY) ? json.get(AUTHOR_KEY).getAsString() : "Unknown";

        ImmutableSet.Builder<ResourceKey<Miraculous>> validMiraculouses = ImmutableSet.builder();
        if (json.has(VALID_MIRACULOUSES_KEY)) {
            for (JsonElement element : json.getAsJsonArray(VALID_MIRACULOUSES_KEY)) {
                validMiraculouses.add(ResourceKey.codec(MineraculousRegistries.MIRACULOUS).parse(JsonOps.INSTANCE, element).getOrThrow());
            }
        }

        if (!json.has(ASSETS_KEY)) throw new IOException("Look missing " + ASSETS_KEY);
        JsonObject contexts = json.getAsJsonObject(ASSETS_KEY);

        Set<String> contextKeys = contexts.keySet();
        if (contextKeys.isEmpty()) throw new IOException("Look " + source + " has no assets");

        ImmutableMap.Builder<ResourceKey<LookContext>, LookAssets> contextAssetsBuilder = new ImmutableMap.Builder<>();
        for (String contextKey : contextKeys) {
            ResourceLocation contextLoc = ResourceLocation.parse(contextKey);
            Holder<LookContext> context = MineraculousBuiltInRegistries.LOOK_CONTEXT.getHolder(contextLoc).orElse(null);
            if (context != null) {
                JsonObject assets = contexts.getAsJsonObject(contextKey);
                Set<String> assetKeys = assets.keySet();
                if (assetKeys.isEmpty()) throw new IOException("Look " + source + " has no assets for context " + contextLoc);

                LookAssets.Builder assetsBuilder = new LookAssets.Builder();
                for (String assetKey : assetKeys) {
                    ResourceLocation assetLoc = ResourceLocation.parse(assetKey);
                    if (!context.value().assetTypes().contains(assetLoc))
                        throw new IllegalArgumentException("Asset type " + assetKey + " not valid for context " + contextLoc);
                    LookAssetType<?, ?> assetType = LookAssetTypes.get(assetLoc);
                    if (assetType == null)
                        throw new IllegalArgumentException("Invalid asset type " + assetKey + " for context " + contextLoc);
                    try {
                        assetsBuilder.add(assetType, assets.get(assetKey), root, hash, contextLoc);
                    } catch (IllegalArgumentException e) {
                        MineraculousConstants.LOGGER.warn("Failed to load asset {} for context {}: {}", assetKey, contextLoc, e.getMessage());
                    }
                }

                LookAssets lookAssets = assetsBuilder.build();
                if (lookAssets.isEmpty()) throw new IOException("Look " + source + " has no assets for context " + contextLoc);

                contextAssetsBuilder.put(context.getKey(), lookAssets);
            }
        }

        ImmutableMap<ResourceKey<LookContext>, LookAssets> contextAssets = contextAssetsBuilder.build();

        if (contextAssets.isEmpty()) {
            throw new IOException("Look '" + source + "' has no assets");
        }

        InternalLookManager.add(source, new Look(hash, displayName, author/*, validMiraculouses.build()*/, contextAssets), equippable);
    }
}
