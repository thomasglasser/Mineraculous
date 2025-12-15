package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
    private static final Path LOOKS_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("looks");
    private static final String JSON_NAME = "look.json";

    private static final String ID_KEY = "id";
    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String AUTHOR_KEY = "author";
    private static final String ASSETS_KEY = "assets";
    private static final String MODEL_KEY = "model";
    private static final String TEXTURE_KEY = "texture";
    private static final String ANIMATIONS_KEY = "animations";
    private static final String TRANSFORMS_KEY = "display";

    private static final int MAX_TEXTURE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 2 * MAX_TEXTURE_SIZE * MAX_TEXTURE_SIZE; // 2MB

    public static void load() {
        LookManager.clear();

        CompletableFuture.runAsync(() -> {
            try {
                if (!Files.exists(LOOKS_PATH))
                    Files.createDirectories(LOOKS_PATH);

                try (Stream<Path> paths = Files.list(LOOKS_PATH)) {
                    paths.forEach(LookLoader::load);
                }
            } catch (Exception e) {
                LookManager.enterSafeMode(e.getMessage());
            }
        });
    }

    private static void load(Path path) {
        if (LookManager.isInSafeMode())
            return;
        try {
            String hash;
            if (Files.isDirectory(path)) {
                Hasher hasher = Hashing.sha256().newHasher();
                try (Stream<Path> stream = Files.walk(path)) {
                    stream.filter(Files::isRegularFile)
                            .sorted(Comparator.comparing(Path::getFileName))
                            .forEach(file -> {
                                try {
                                    hasher.putBytes(Files.readAllBytes(file));
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to hash file: " + file, e);
                                }
                            });
                }
                hash = hasher.hash().toString();
                loadFromRoot(path, hash);
            } else if (path.toString().endsWith(".zip")) {
                try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                    loadFromRoot(fs.getPath("/"), com.google.common.io.Files.asByteSource(path.toFile()).hash(Hashing.sha256()).toString());
                }
            }
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load look from {}: {}", path, e.getMessage());
        }
    }

    private static void loadFromRoot(Path root, String hash) throws Exception {
        if (LookManager.isInSafeMode())
            return;

        Path path = root.resolve(JSON_NAME);
        if (!Files.exists(path))
            throw new FileNotFoundException("Missing " + JSON_NAME + " in " + root);

        JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        if (!json.has(ID_KEY)) throw new IOException("Look missing '" + ID_KEY + "'");
        String id = json.get(ID_KEY).getAsString();
        if (id.isEmpty()) throw new IOException("Look ID cannot be empty");

        if (!json.has(DISPLAY_NAME_KEY)) throw new IOException("Look missing '" + DISPLAY_NAME_KEY + "'");
        String displayName = json.get(DISPLAY_NAME_KEY).getAsString();

        String author = json.has(AUTHOR_KEY) ? json.get(AUTHOR_KEY).getAsString() : "Unknown";

        EnumMap<MiraculousLook.AssetType, BakedGeoModel> models = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, ResourceLocation> textures = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, BakedAnimations> animations = new EnumMap<>(MiraculousLook.AssetType.class);
        EnumMap<MiraculousLook.AssetType, ItemTransforms> transforms = new EnumMap<>(MiraculousLook.AssetType.class);

        if (!json.has(ASSETS_KEY)) throw new IOException("Look missing '" + ASSETS_KEY + "'");
        JsonObject assets = json.getAsJsonObject(ASSETS_KEY);

        Set<String> keys = assets.keySet();
        if (keys.isEmpty()) throw new IOException("Look '" + id + "' has no assets");

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
                    ResourceLocation texture = registerTexture(root, asset, TEXTURE_KEY, id, assetType, hash);
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

        if (models.isEmpty() && textures.isEmpty() && animations.isEmpty()) {
            throw new IOException("Look '" + id + "' has no assets");
        }

        LookManager.add(new MiraculousLook(id, hash, displayName, author, false, models, textures, animations, transforms));
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
            if (Files.size(path) > MAX_FILE_SIZE)
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

    private static @Nullable ResourceLocation registerTexture(Path root, JsonObject asset, String key, String id, MiraculousLook.AssetType assetType, String hash) throws Exception {
        Path path = findValidPath(root, asset, key);
        if (path != null) {
            return registerTexture(NativeImage.read(path.toUri().toURL().openStream()), id, assetType, hash);
        }
        return null;
    }

    private static @Nullable BakedGeoModel readModel(JsonObject object) {
        return BakedModelFactory.getForNamespace(MineraculousConstants.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(object, Model.class)));
    }

    private static @Nullable ResourceLocation registerTexture(NativeImage image, String id, MiraculousLook.AssetType assetType, String hash) {
        if (image.getWidth() > MAX_TEXTURE_SIZE || image.getHeight() > MAX_TEXTURE_SIZE) {
            MineraculousConstants.LOGGER.warn("Look texture for look {} is too large ({}x{}). Max is {}x{}.", id, image.getWidth(), image.getHeight(), MAX_TEXTURE_SIZE, MAX_TEXTURE_SIZE);
            image.close();
            return null;
        }

        ResourceLocation texture = MineraculousConstants.modLoc("textures/looks/" + id + "_" + assetType.getSerializedName() + "_" + hash + ".png");
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
