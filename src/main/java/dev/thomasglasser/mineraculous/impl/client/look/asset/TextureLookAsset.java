package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import dev.thomasglasser.mineraculous.impl.client.renderer.texture.DynamicAutoGlowingTexture;
import dev.thomasglasser.mineraculous.impl.core.look.LookLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class TextureLookAsset implements LookAssetType<String, ResourceLocation> {
    public static final TextureLookAsset INSTANCE = new TextureLookAsset();

    protected TextureLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.TEXTURE;
    }

    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public ResourceLocation load(String asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException {
        return load(LookUtils.findValidPath(root, asset), lookId, key(), contextId, "");
    }

    @Override
    public Supplier<ResourceLocation> getBuiltIn(String asset, ResourceLocation lookId) {
        ResourceLocation location = ResourceLocation.parse(asset);
        return () -> location;
    }

    public static ResourceLocation load(Path imagePath, ResourceLocation lookId, ResourceLocation key, ResourceLocation context, String textureSuffix) throws IOException, IllegalArgumentException {
        NativeImage image = NativeImage.read(Files.newInputStream(imagePath));
        verifyImage(image);

        ResourceLocation texture = lookId.withPath(path -> "textures/" + LookUtils.toString(LookLoader.LOOKS_SUBPATH) + "/" + path + "/" + LookUtils.toShortPath(context) + "/" + LookUtils.toShortPath(key) + textureSuffix + ".png");
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));

        Path glowmaskPath = imagePath.resolveSibling(imagePath.getFileName().toString().replace(".png", "_glowmask.png"));
        if (Files.exists(glowmaskPath)) {
            try (InputStream glowmaskStream = Files.newInputStream(glowmaskPath)) {
                byte[] glowmask = glowmaskStream.readAllBytes();
                try (NativeImage glowmaskImage = NativeImage.read(glowmask)) {
                    verifyImage(glowmaskImage);
                }
                Minecraft.getInstance().getTextureManager().register(texture.withPath(path -> path.replace(".png", "_glowmask.png")), new DynamicAutoGlowingTexture(texture, glowmask));
            }
        }

        return texture;
    }

    private static void verifyImage(NativeImage image) {
        if (image.getWidth() > LookLoader.MAX_TEXTURE_SIZE || image.getHeight() > LookLoader.MAX_TEXTURE_SIZE) {
            image.close();
            throw new IllegalArgumentException("Look texture is too large (" + image.getWidth() + "x" + image.getHeight() + "). Max is " + LookLoader.MAX_TEXTURE_SIZE + "x" + LookLoader.MAX_TEXTURE_SIZE);
        }
    }
}
