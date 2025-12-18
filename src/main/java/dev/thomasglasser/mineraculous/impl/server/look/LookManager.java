package dev.thomasglasser.mineraculous.impl.server.look;

import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    public static final Path LOOKS_SUBPATH = Path.of(MineraculousConstants.MOD_ID, "looks");
    public static final Path CACHE_SUBPATH = LOOKS_SUBPATH.resolve(".cache");
    public static final int MAX_TEXTURE_SIZE = 1024;
    public static final int MAX_FILE_SIZE = 2 * MAX_TEXTURE_SIZE * MAX_TEXTURE_SIZE; // 2MB

    private static final Map<String, Path> LOOKS = new ConcurrentHashMap<>();

    private static Path cachePath;

    public static void init(MinecraftServer server) {
        cachePath = server.getWorldPath(LevelResource.ROOT).resolve(CACHE_SUBPATH);

        try {
            clearOrCreateCache(cachePath);

            try (Stream<Path> stream = Files.list(cachePath)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String filename = path.getFileName().toString();
                            String hash = com.google.common.io.Files.getNameWithoutExtension(filename);
                            LOOKS.put(hash, path);
                        });
            }
        } catch (IOException e) {
            MineraculousConstants.LOGGER.error("Failed to initialize server look storage", e);
        }
    }

    public static void clearOrCreateCache(Path cachePath) throws IOException {
        if (Files.exists(cachePath))
            MoreFiles.deleteDirectoryContents(cachePath, RecursiveDeleteOption.ALLOW_INSECURE);
        else
            Files.createDirectories(cachePath);
    }

    public static void ensureCacheExists(Path cachePath) throws IOException {
        if (!Files.exists(cachePath))
            Files.createDirectories(cachePath);
    }

    public static boolean hasLook(String hash) {
        return LOOKS.containsKey(hash);
    }

    public static byte @Nullable [] getLookData(String hash) {
        Path path = LOOKS.get(hash);
        if (path != null && Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                MineraculousConstants.LOGGER.error("Failed to read look file: {}", path, e);
            }
        }
        return null;
    }

    public static void saveLook(String hash, byte[] data) {
        if (data.length > MAX_FILE_SIZE) {
            MineraculousConstants.LOGGER.warn("Rejected upload: Look {} is too large ({} bytes)", hash, data.length);
            return;
        }

        String calculatedHash = Hashing.sha256().hashBytes(data).toString();
        if (!calculatedHash.equals(hash)) {
            MineraculousConstants.LOGGER.warn("Rejected upload: Hash mismatch for {}. Calculated: {}", hash, calculatedHash);
            return;
        }

        Path path = cachePath.resolve(hash + ".look");
        try {
            Files.write(path, data);
            LOOKS.put(hash, path);
            MineraculousConstants.LOGGER.info("Saved new look: {}", hash);
        } catch (IOException e) {
            MineraculousConstants.LOGGER.error("Failed to save look file: {}", path, e);
        }
    }
}
