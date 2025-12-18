package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public class LookLoader {
    public static final Path LOOKS_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve(ServerLookManager.LOOKS_SUBPATH);
    public static final Path CACHE_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve(ServerLookManager.CACHE_SUBPATH);

    private static final String JSON_NAME = "look.json";

    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String AUTHOR_KEY = "author";
    private static final String VALID_MIRACULOUSES_KEY = "valid_miraculouses";
    private static final String ASSETS_KEY = "assets";
    private static final String MODEL_KEY = "model";
    private static final String TEXTURE_KEY = "texture";
    private static final String ANIMATIONS_KEY = "animations";
    private static final String TRANSFORMS_KEY = "display";

    public static void load() {
        LookManager.refresh();

        CompletableFuture.runAsync(() -> {
            try {
                ServerLookManager.clearOrCreateCache(CACHE_PATH);

                try (Stream<Path> paths = Files.list(LOOKS_PATH)) {
                    paths.filter(path -> !path.equals(CACHE_PATH)).forEach(LookLoader::load);
                }
            } catch (Exception e) {
                LookManager.enterSafeMode(e.getMessage());
            }
        });
    }

    public static void load(Path path) {
        if (LookManager.isInSafeMode())
            return;
        try {
            String hash;
            if (Files.isDirectory(path)) {
                try {
                    byte[] zipBytes = zipFolderToBytes(path);
                    hash = Hashing.sha256().hashBytes(zipBytes).toString();
                    ServerLookManager.ensureCacheExists(CACHE_PATH);
                    Path look = CACHE_PATH.resolve(hash + ".look");
                    Files.write(look, zipBytes);
                    load(look);
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.error("Failed to parse look folder {}: {}", path, e.getMessage());
                }
            } else if (path.toString().endsWith(".zip") || path.toString().endsWith(".look")) {
                try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                    loadFromRoot(fs.getPath("/"), path, com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha256()).toString());
                }
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load look from {}: {}", path, e.getMessage());
        }
    }

    public static byte[] zipFolderToBytes(Path sourceFolder) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(byteStream)) {
            try (Stream<Path> paths = Files.walk(sourceFolder)) {
                paths.filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(sourceFolder.relativize(path).toString().replace('\\', '/'));
                            try {
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

    private static void loadFromRoot(Path root, Path source, String hash) throws Exception {
        if (LookManager.isInSafeMode())
            return;

        Path path = root.resolve(JSON_NAME);
        if (!Files.exists(path))
            throw new FileNotFoundException("Missing " + JSON_NAME + " in " + root);

        JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        if (!json.has(DISPLAY_NAME_KEY)) throw new IOException("Look missing '" + DISPLAY_NAME_KEY + "'");
        String displayName = json.get(DISPLAY_NAME_KEY).getAsString();

        String author = json.has(AUTHOR_KEY) ? json.get(AUTHOR_KEY).getAsString() : "Unknown";

        ImmutableSet.Builder<ResourceKey<Miraculous>> validMiraculouses = ImmutableSet.builder();
        if (json.has(VALID_MIRACULOUSES_KEY)) {
            for (JsonElement element : json.getAsJsonArray(VALID_MIRACULOUSES_KEY)) {
                validMiraculouses.add(ResourceKey.codec(MineraculousRegistries.MIRACULOUS).parse(JsonOps.INSTANCE, element).getOrThrow());
            }
        }

        EnumMap<MiraculousLook.AssetType, BakedGeoModel> models = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, ResourceLocation> textures = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, BakedAnimations> animations = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, ItemTransforms> transforms = new EnumMap<>(MiraculousLook.AssetType.class);

        if (!json.has(ASSETS_KEY)) throw new IOException("Look missing '" + ASSETS_KEY + "'");
        JsonObject assets = json.getAsJsonObject(ASSETS_KEY);

        Set<String> keys = assets.keySet();
        if (keys.isEmpty()) throw new IOException("Look '" + source + "' has no assets");

        for (String key : keys) {
            MiraculousLook.AssetType assetType = MiraculousLook.AssetType.of(key);
            if (assetType != null) {
                JsonObject asset = assets.getAsJsonObject(key);
                if (asset.has(MODEL_KEY)) {
                    BakedGeoModel model = read(root, asset, MODEL_KEY, LookLoader::readModel);
                    if (model != null)
                        models.put(assetType, model);
                }
                if (asset.has(TEXTURE_KEY)) {
                    ResourceLocation texture = registerTexture(root, asset, TEXTURE_KEY, hash, assetType, source);
                    if (texture != null)
                        textures.put(assetType, texture);
                }
                if (asset.has(ANIMATIONS_KEY)) {
                    BakedAnimations animation = read(root, asset, ANIMATIONS_KEY, LookLoader::readAnimations);
                    if (animation != null)
                        animations.put(assetType, animation);
                }
                if (assetType.hasTransforms() && asset.has(TRANSFORMS_KEY)) {
                    ItemTransforms itemTransforms = read(root, asset, TRANSFORMS_KEY, LookLoader::readTransforms);
                    if (itemTransforms != null)
                        transforms.put(assetType, itemTransforms);
                }
            }
        }

        if (models.isEmpty() && textures.isEmpty() && animations.isEmpty() && transforms.isEmpty()) {
            throw new IOException("Look '" + source + "' has no assets");
        }

        LookManager.add(source, new MiraculousLook(hash, false, displayName, author, validMiraculouses.build(), models, textures, animations, transforms));
    }

    private static @Nullable Path findValidPath(Path root, JsonObject asset, String key) throws Exception {
        if (asset.has(key)) {
            String file = asset.get(key).getAsString();
            Path path = root.resolve(file);

            if (!path.normalize().startsWith(root.normalize())) {
                throw new IOException("Invalid path (Zip Slip attempt): " + path);
            }
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Referenced file not found: " + file);
            }
            if (Files.size(path) > ServerLookManager.MAX_FILE_SIZE)
                throw new IOException("File too large, must be <=2MB: " + file);

            return path;
        }
        return null;
    }

    private static <T> @Nullable T read(Path root, JsonObject asset, String key, Function<JsonObject, @Nullable T> reader) throws Exception {
        Path path = findValidPath(root, asset, key);
        if (path != null)
            return reader.apply(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
        return null;
    }

    private static @Nullable ResourceLocation registerTexture(Path root, JsonObject asset, String key, String hash, MiraculousLook.AssetType assetType, Path source) throws Exception {
        Path path = findValidPath(root, asset, key);
        if (path != null) {
            return registerTexture(NativeImage.read(Files.newInputStream(path)), hash, assetType, source);
        }
        return null;
    }

    private static @Nullable BakedGeoModel readModel(JsonObject object) {
        return BakedModelFactory.getForNamespace(MineraculousConstants.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(object, Model.class)));
    }

    private static @Nullable ResourceLocation registerTexture(NativeImage image, String hash, MiraculousLook.AssetType assetType, Path source) {
        if (image.getWidth() > ServerLookManager.MAX_TEXTURE_SIZE || image.getHeight() > ServerLookManager.MAX_TEXTURE_SIZE) {
            MineraculousConstants.LOGGER.warn("Look texture for look {} is too large ({}x{}). Max is {}x{}.", source, image.getWidth(), image.getHeight(), ServerLookManager.MAX_TEXTURE_SIZE, ServerLookManager.MAX_TEXTURE_SIZE);
            image.close();
            return null;
        }

        ResourceLocation texture = MineraculousConstants.modLoc("textures/looks/" + hash + "_" + assetType.getSerializedName() + "_.png");
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
        return texture;
    }

    private static @Nullable BakedAnimations readAnimations(JsonObject object) {
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(object, "animations"), BakedAnimations.class);
    }

    private static @Nullable ItemTransforms readTransforms(JsonObject object) {
        if (object.has(TRANSFORMS_KEY))
            return BlockModel.fromString(object.toString()).getTransforms();
        return null;
    }
}
