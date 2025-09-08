package dev.thomasglasser.mineraculous.api.client.renderer.layer;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
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

    @Override
    protected @Nullable RenderType getRenderType(T animatable, @Nullable MultiBufferSource bufferSource) {
        ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(renderer.getTextureLocation(animatable), "_glowmask");
        if (PRESENT_GLOWMASKS.computeIfAbsent(glowmaskTexture, (ResourceLocation texture) -> Minecraft.getInstance().getResourceManager().getResource(texture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(texture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture())) {
            return super.getRenderType(animatable, bufferSource);
        } else {
            return null;
        }
    }

    public static void clearGlowmasks() {
        PRESENT_GLOWMASKS.clear();
    }
}
