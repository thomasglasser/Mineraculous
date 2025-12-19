package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
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
    public ResourceLocation load(Path path, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        NativeImage image = NativeImage.read(Files.newInputStream(path));
        if (image.getWidth() > ServerLookManager.MAX_TEXTURE_SIZE || image.getHeight() > ServerLookManager.MAX_TEXTURE_SIZE) {
            image.close();
            throw new IllegalArgumentException("Look texture is too large (" + image.getWidth() + "x" + image.getHeight() + "). Max is " + ServerLookManager.MAX_TEXTURE_SIZE + "x" + ServerLookManager.MAX_TEXTURE_SIZE);
        }

        ResourceLocation texture = MineraculousConstants.modLoc("textures/looks/" + hash + "_" + context.getNamespace() + "_" + context.getPath() + "_.png");
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
        return texture;
    }
}
