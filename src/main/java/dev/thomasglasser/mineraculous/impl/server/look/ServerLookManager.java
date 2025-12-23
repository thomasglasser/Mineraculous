package dev.thomasglasser.mineraculous.impl.server.look;

import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundRequestLooksPayload;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSendLookPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

public class ServerLookManager {
    public static final Path LOOKS_SUBPATH = Path.of(MineraculousConstants.MOD_ID, "looks");
    public static final Path CACHE_SUBPATH = LOOKS_SUBPATH.resolve(".cache");
    public static final int MAX_TEXTURE_SIZE = 1024;
    public static final int MAX_FILE_SIZE = 2 * MAX_TEXTURE_SIZE * MAX_TEXTURE_SIZE; // 2MB

    private static final Map<String, StoredLook> LOOKS = new ConcurrentHashMap<>();

    private static Path looksPath;
    private static Path cachePath;

    public static void init(MinecraftServer server) {
        LOOKS.clear();
        looksPath = server.getWorldPath(LevelResource.ROOT).resolve(LOOKS_SUBPATH);
        cachePath = server.getWorldPath(LevelResource.ROOT).resolve(CACHE_SUBPATH);

        CompletableFuture.runAsync(() -> {
            try {
                clearOrCreateCache(cachePath);

                try (Stream<Path> paths = Files.list(looksPath)) {
                    paths.forEach(path -> {
                        if (path.equals(cachePath))
                            return;
                        load(path, !path.startsWith(cachePath));
                    });
                }
            } catch (IOException e) {
                MineraculousConstants.LOGGER.error("Failed to initialize server look storage", e);
            }
        });
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

    public static void requestMissingLooks(LookData lookData, ServerPlayer player) {
        Set<String> missing = new ObjectOpenHashSet<>();
        for (String hash : lookData.hashes().values()) {
            if (!hasLook(hash)) {
                missing.add(hash);
            }
        }
        if (!missing.isEmpty())
            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestLooksPayload(missing), player);
    }

    public static void sendServerLooks(ServerPlayer player) {
        for (Map.Entry<String, StoredLook> entry : LOOKS.entrySet()) {
            String hash = entry.getKey();
            TommyLibServices.NETWORK.sendToClient(new ClientboundSendLookPayload(hash, getLookData(hash)), player);
        }
    }

    public static boolean hasLook(String hash) {
        return LOOKS.containsKey(hash);
    }

    @Nullable
    public static ServerLook getLookData(String hash) {
        StoredLook look = LOOKS.get(hash);
        if (look != null) {
            Path path = look.path();
            if (Files.exists(path)) {
                try {
                    return new ServerLook(Files.readAllBytes(path), look.equippable());
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.error("Failed to read look file: {}", path, e);
                }
            }
        }
        return null;
    }

    public static void saveLook(String hash, byte[] data, boolean equippable) {
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
            LOOKS.put(hash, new StoredLook(path, equippable));
            MineraculousConstants.LOGGER.info("Saved new look: {}", hash);
        } catch (IOException e) {
            MineraculousConstants.LOGGER.error("Failed to save look file: {}", path, e);
        }
    }

    public static byte[] zipFolderToBytes(Path sourceFolder) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(byteStream)) {
            try (Stream<Path> paths = Files.walk(sourceFolder).sorted()) {
                paths.filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                ZipEntry zipEntry = new ZipEntry(sourceFolder.relativize(path).toString().replace('\\', '/'));

                                zipEntry.setTime(0);
                                zipStream.putNextEntry(zipEntry);
                                Files.copy(path, zipStream);
                                zipStream.closeEntry();
                            } catch (IOException e) {
                                MineraculousConstants.LOGGER.warn("Failed to zip look file: {}", path, e);
                            }
                        });
            }
        }
        return byteStream.toByteArray();
    }

    private static void load(Path path, boolean equippable) {
        try {
            String hash;
            if (Files.isDirectory(path)) {
                try {
                    byte[] zipBytes = zipFolderToBytes(path);
                    hash = Hashing.sha256().hashBytes(zipBytes).toString();
                    ServerLookManager.ensureCacheExists(cachePath);
                    Path cachedPath = cachePath.resolve(hash + ".look");
                    Files.write(cachedPath, zipBytes);
                    load(cachedPath, equippable);
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.error("Failed to parse look folder {}: {}", path, e.getMessage());
                }
            } else if (path.toString().endsWith(".zip")) {
                hash = com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha256()).toString();
                Path cachedPath = cachePath.resolve(hash + ".look");
                Files.copy(path, cachedPath, StandardCopyOption.REPLACE_EXISTING);
                load(cachedPath, equippable);
            } else if (path.toString().endsWith(".look")) {
                LOOKS.put(com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha256()).toString(), new StoredLook(path, equippable));
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load look from {}: {}", path, e.getMessage());
        }
    }

    public record ServerLook(byte[] data, boolean equippable) {
        public static final StreamCodec<ByteBuf, ServerLook> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE_ARRAY, ServerLook::data,
                ByteBufCodecs.BOOL, ServerLook::equippable,
                ServerLook::new);
    }

    private record StoredLook(Path path, boolean equippable) {}
}
