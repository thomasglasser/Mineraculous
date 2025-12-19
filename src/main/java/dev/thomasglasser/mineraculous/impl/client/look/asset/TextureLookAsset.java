package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class TextureLookAsset implements LookAssetType<ResourceLocation> {
    public static final TextureLookAsset INSTANCE = new TextureLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("texture");

    private TextureLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public ResourceLocation load(JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return load(LookManager.findValidPath(root, asset.getAsString()), "textures/looks/" + hash + "_" + context.getNamespace() + "_" + context.getPath());
    }

    public static ResourceLocation load(Path path, String texturePath) throws IOException, IllegalArgumentException {
        NativeImage image = NativeImage.read(Files.newInputStream(path));
        verifyImage(image);

        ResourceLocation texture = MineraculousConstants.modLoc(texturePath + ".png");
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
        return texture;
    }

    private static void verifyImage(NativeImage image) {
        if (image.getWidth() > ServerLookManager.MAX_TEXTURE_SIZE || image.getHeight() > ServerLookManager.MAX_TEXTURE_SIZE) {
            image.close();
            throw new IllegalArgumentException("Look texture is too large (" + image.getWidth() + "x" + image.getHeight() + "). Max is " + ServerLookManager.MAX_TEXTURE_SIZE + "x" + ServerLookManager.MAX_TEXTURE_SIZE);
        }
    }
}
