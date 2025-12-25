package dev.thomasglasser.mineraculous.api.core.look;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.core.look.LookLoader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

public class LookUtils {
    /**
     * Converts a key to a look id,
     * using the registry and location.
     *
     * @param key The key to convert
     * @return The converted id
     */
    public static ResourceLocation getDefaultLookId(ResourceKey<?> key) {
        return key.location().withPath(path -> toShortPath(key.registry()) + "/" + path);
    }

    /**
     * Converts a path to a string,
     * with forward slashes for MC compatibility.
     *
     * @param path The path to convert
     * @return The converted path
     */
    public static String toString(Path path) {
        return path.toString().replace('\\', '/');
    }

    /**
     * Converts the provided id to a path,
     * omitting the namespace if it is {@link ResourceLocation#DEFAULT_NAMESPACE}.
     *
     * @param id The id to convert
     * @return The converted path
     */
    public static String toShortPath(ResourceLocation id) {
        return id.toShortLanguageKey().replace('.', '/');
    }

    /**
     * Finds the path to the provided location and ensures it is valid.
     *
     * @param root  The root path to search in
     * @param value The value to search for
     * @return The found path
     * @throws IOException If the path is invalid
     */
    public static Path findValidPath(Path root, String value) throws IOException {
        Path path = root.resolve(value);

        if (!path.normalize().startsWith(root.normalize())) {
            throw new IOException("Invalid path (Zip Slip attempt): " + value);
        }
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Referenced file not found: " + value);
        }
        if (Files.size(path) > LookLoader.MAX_FILE_SIZE)
            throw new IOException("File too large, must be <=2MB: " + value);

        return path;
    }

    @ApiStatus.Internal
    public static void clearOrCreateCache(Path cachePath) throws IOException {
        if (Files.exists(cachePath))
            MoreFiles.deleteDirectoryContents(cachePath, RecursiveDeleteOption.ALLOW_INSECURE);
        else
            Files.createDirectories(cachePath);
    }

    @ApiStatus.Internal
    public static void ensureCacheExists(Path cachePath) throws IOException {
        if (!Files.exists(cachePath))
            Files.createDirectories(cachePath);
    }

    @ApiStatus.Internal
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
}
