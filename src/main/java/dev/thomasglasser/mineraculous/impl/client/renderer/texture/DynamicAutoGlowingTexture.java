package dev.thomasglasser.mineraculous.impl.client.renderer.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
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
    private final byte[] image;

    public DynamicAutoGlowingTexture(ResourceLocation base, byte[] image) {
        super(base, ResourceLocation.withDefaultNamespace(""));
        this.image = image;
    }

    @Override
    protected @Nullable RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
        AbstractTexture originalTexture = mc.getTextureManager().getTexture(this.textureBase);

        if (!(originalTexture instanceof DynamicTexture dynamicTexture)) {
            throw new IllegalArgumentException("Tried to render a dynamic glowing texture for non-dynamic original texture " + textureBase);
        }

        NativeImage baseImage = dynamicTexture.getPixels();
        NativeImage glowImage = NativeImage.read(image);

        GeoGlowingTextureMeta glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
        glowLayerMeta.createImageMask(baseImage, glowImage);

        if (PRINT_DEBUG_IMAGES && GeckoLibServices.PLATFORM.isDevelopmentEnvironment()) {
            printDebugImageToDisk(this.textureBase, baseImage);
            printDebugImageToDisk(this.glowLayer, glowImage);
        }

        if (baseImage != null && (glowImage.getWidth() != baseImage.getWidth() || glowImage.getHeight() != baseImage.getHeight()))
            throw new IllegalStateException(String.format("Glowmask texture dimensions do not match base texture dimensions! Base: %s", this.textureBase));

        return () -> {
            uploadSimple(getId(), glowImage, blur, false);
            dynamicTexture.upload();
            glowImage.close();
        };
    }
}
