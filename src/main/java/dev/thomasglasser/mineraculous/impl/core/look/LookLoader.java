package dev.thomasglasser.mineraculous.impl.core.look;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.asset.BuiltInLookAssets;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.impl.client.look.ClientLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;
import software.bernie.geckolib.loading.FileLoader;

public class LookLoader {
    public static final Path LOOKS_SUBPATH = Path.of(MineraculousConstants.MOD_ID, "looks");
    public static final Path CACHE_SUBPATH = LOOKS_SUBPATH.resolve(".cache");
    public static final Path LOOKS_DIR = FMLPaths.GAMEDIR.get().resolve(LOOKS_SUBPATH);
    public static final Path CACHE_DIR = FMLPaths.GAMEDIR.get().resolve(CACHE_SUBPATH);
    public static final int MAX_TEXTURE_SIZE = 1024;
    public static final int MAX_FILE_SIZE = 2 * MAX_TEXTURE_SIZE * MAX_TEXTURE_SIZE; // 2MB

    public static final String JSON_NAME = "look.json";

    public static CompletableFuture<Void> loadBuiltIn(PreparableReloadListener.PreparationBarrier stage, ResourceManager resourceManager, Executor backgroundExecutor,
            Executor gameExecutor) {
        ImmutableMap.Builder<ResourceLocation, Look<BuiltInLookAssets>> map = new ImmutableMap.Builder<>();
        return loadBuiltIn(backgroundExecutor, resourceManager, map::put)
                .thenCompose(stage::wait)
                .thenAcceptAsync(empty -> ClientLookManager.setBuiltIn(map.build()), gameExecutor);
    }

    private static CompletableFuture<Void> loadBuiltIn(Executor executor, ResourceManager resourceManager, BiConsumer<ResourceLocation, Look<BuiltInLookAssets>> map) {
        return CompletableFuture.runAsync(() -> {
            String looksPathString = LookUtils.toString(LOOKS_SUBPATH);
            CompletableFuture.supplyAsync(
                    () -> resourceManager.listResources(looksPathString, fileName -> fileName.toString().endsWith(".json")), executor)
                    .thenApplyAsync(resources -> {
                        Map<ResourceLocation, CompletableFuture<Look<BuiltInLookAssets>>> tasks = new Object2ObjectOpenHashMap<>();

                        for (ResourceLocation lookLoc : resources.keySet()) {
                            ResourceLocation lookId = lookLoc.withPath(path -> path.substring(path.indexOf(looksPathString) + looksPathString.length() + 1, path.indexOf(".json")));
                            tasks.put(lookId, CompletableFuture.supplyAsync(() -> Look.load(FileLoader.loadFile(lookLoc, resourceManager), lookId, BuiltInLookAssets.Builder::new), executor));
                        }

                        return tasks;
                    }, executor)
                    .thenAcceptAsync(tasks -> {
                        for (Map.Entry<ResourceLocation, CompletableFuture<Look<BuiltInLookAssets>>> entry : tasks.entrySet()) {
                            map.accept(entry.getKey(), entry.getValue().join());
                        }
                    }, executor);
        });
    }

    public static void loadLoaded(LookProcessor lookProcessor) {
        try {
            LookUtils.clearOrCreateCache(LookLoader.CACHE_DIR);

            try (Stream<Path> paths = Files.list(LOOKS_DIR)) {
                paths.forEach(path -> {
                    if (path.equals(CACHE_DIR))
                        return;
                    loadLoaded(path, true, lookProcessor);
                });
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load looks: {}", e.getMessage());
        }
    }

    public static void loadLoaded(Path source, boolean equippable, LookProcessor lookProcessor) {
        try {
            if (Files.isDirectory(source)) {
                try {
                    byte[] zipBytes = LookUtils.zipFolderToBytes(source);
                    LookUtils.ensureCacheExists(CACHE_DIR);
                    Path look = CACHE_DIR.resolve(Hashing.sha256().hashBytes(zipBytes) + ".look");
                    Files.write(look, zipBytes);
                    loadLoaded(look, equippable, lookProcessor);
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.error("Failed to parse look folder {}: {}", source, e.getMessage());
                }
            } else if (source.toString().endsWith(".zip")) {
                Path cachedPath = LookLoader.CACHE_DIR.resolve(Hashing.sha256().hashBytes(Files.readAllBytes(source)) + ".look");
                Files.copy(source, cachedPath, StandardCopyOption.REPLACE_EXISTING);
                loadLoaded(cachedPath, equippable, lookProcessor);
            } else if (source.toString().endsWith(".look")) {
                try (FileSystem fs = FileSystems.newFileSystem(source, (ClassLoader) null)) {
                    Path root = fs.getPath("/");
                    try (Stream<Path> paths = Files.list(root)) {
                        paths.forEach(path -> {
                            if (path.getFileName().toString().equals(LookLoader.JSON_NAME)) {
                                try {
                                    lookProcessor.process(Hashing.sha256().hashBytes(Files.readAllBytes(source)).toString(), root, source, path, equippable);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load look from {}: {}", source, e.getMessage());
        }
    }

    public interface LookProcessor {
        void process(String hash, Path root, Path source, Path file, boolean equippable) throws IOException;
    }
}
