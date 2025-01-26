package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.resource.GeoGlowingTextureMeta;

public class DynamicAutoGlowingTexture extends AutoGlowingTexture {
    private final NativeImage glowImage;

    public DynamicAutoGlowingTexture(ResourceLocation textureBase, byte[] glowImage) throws IOException {
        super(textureBase, appendToPath(textureBase, "_glowmask"));
        this.glowImage = NativeImage.read(glowImage);
    }

    public static void register(ResourceLocation textureBase, byte[] glowImage) throws IOException {
        DynamicAutoGlowingTexture dynamicAutoGlowingTexture = new DynamicAutoGlowingTexture(textureBase, glowImage);
        Minecraft.getInstance().getTextureManager().register(dynamicAutoGlowingTexture.glowLayer, dynamicAutoGlowingTexture);
    }

    @Override
    protected @Nullable RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
        AbstractTexture originalTexture;

        try {
            originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(this.textureBase)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to load original texture: " + this.textureBase, e);
        }

        NativeImage baseImage = originalTexture instanceof DynamicTexture dynamicTexture ? dynamicTexture.getPixels() : null;

        if (baseImage == null)
            return null;

        GeoGlowingTextureMeta glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);

        glowLayerMeta.createImageMask(baseImage, glowImage);

        if (PRINT_DEBUG_IMAGES && GeckoLibServices.PLATFORM.isDevelopmentEnvironment()) {
            printDebugImageToDisk(this.textureBase, baseImage);
            printDebugImageToDisk(this.glowLayer, glowImage);
        }

        NativeImage mask = glowImage;

        return () -> {
            uploadSimple(getId(), mask, blur, false);

            if (originalTexture instanceof DynamicTexture dynamicTexture) {
                dynamicTexture.upload();
            } else {
                uploadSimple(originalTexture.getId(), baseImage, blur, false);
            }
        };
    }
}
