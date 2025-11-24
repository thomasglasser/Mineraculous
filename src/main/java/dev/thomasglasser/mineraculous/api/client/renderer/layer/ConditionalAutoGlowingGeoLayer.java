package dev.thomasglasser.mineraculous.api.client.renderer.layer;

import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ConditionalAutoGlowingGeoLayer<T extends GeoAnimatable> extends AutoGlowingGeoLayer<T> {
    private static final Object2BooleanMap<ResourceLocation> PRESENT_GLOWMASKS = new Object2BooleanOpenHashMap<>();

    public ConditionalAutoGlowingGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    public static void clearGlowmasks() {
        PRESENT_GLOWMASKS.clear();
    }

    @Override
    protected @Nullable RenderType getRenderType(T animatable, @Nullable MultiBufferSource bufferSource) {
        if (PRESENT_GLOWMASKS.computeIfAbsent(renderer.getTextureLocation(animatable), (ResourceLocation original) -> MineraculousClientUtils.isValidTexture(AutoGlowingTexture.appendToPath(original, "_glowmask")))) {
            return super.getRenderType(animatable, bufferSource);
        } else {
            return null;
        }
    }
}
