package dev.thomasglasser.mineraculous.api.client.renderer.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.resource.GeoGlowingTextureMeta;

public class DynamicAutoGlowingTexture extends AutoGlowingTexture {
    private final NativeImage pixels;

    public DynamicAutoGlowingTexture(ResourceLocation textureBase, byte[] pixels) throws IOException {
        super(textureBase, appendToPath(textureBase, "_glowmask"));
        this.pixels = NativeImage.read(pixels);
    }

    public static void register(ResourceLocation textureBase, byte[] glowImage) throws IOException {
        DynamicAutoGlowingTexture dynamicAutoGlowingTexture = new DynamicAutoGlowingTexture(textureBase, glowImage);
        Minecraft.getInstance().getTextureManager().register(dynamicAutoGlowingTexture.glowLayer, dynamicAutoGlowingTexture);
    }

    @Override
    protected @Nullable RenderCall loadTexture(ResourceManager resourceManager, Minecraft minecraft) throws IOException {
        AbstractTexture originalTexture;

        try {
            originalTexture = minecraft.submit(() -> minecraft.getTextureManager().getTexture(this.textureBase)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to load original texture: " + this.textureBase, e);
        }

        if (originalTexture instanceof DynamicTexture dynamicTexture) {
            NativeImage baseImage = dynamicTexture.getPixels();
            GeoGlowingTextureMeta glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(pixels);

            glowLayerMeta.createImageMask(baseImage, pixels);

            if (PRINT_DEBUG_IMAGES && TommyLibServices.PLATFORM.isDevelopmentEnvironment()) {
                printDebugImageToDisk(this.textureBase, baseImage);
                printDebugImageToDisk(this.glowLayer, pixels);
            }

            NativeImage mask = pixels;

            return () -> {
                uploadSimple(getId(), mask, blur, false);
                dynamicTexture.upload();
            };
        }
        return null;
    }
}
