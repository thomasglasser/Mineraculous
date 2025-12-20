package dev.thomasglasser.mineraculous.api.client.model;

import dev.thomasglasser.mineraculous.api.client.look.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;

/**
 * A {@link GeoModel} that uses the look system for the model, texture, and animation.
 * 
 * @param <T> The type of animatable
 */
public class LookGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final LookRenderer renderer;

    private BakedGeoModel currentModel = null;

    public LookGeoModel(LookRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return null;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }

    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel model = renderer.getAssetOrDefault(LookAssetTypes.GECKOLIB_MODEL);
        if (model != this.currentModel) {
            this.getAnimationProcessor().setActiveModel(model);
            this.currentModel = model;
        }
        return currentModel;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return renderer.getAssetOrDefault(LookAssetTypes.TEXTURE);
    }

    @Override
    public @Nullable Animation getAnimation(T animatable, String name) {
        return findAnimation(name, renderer.getAsset(LookAssetTypes.GECKOLIB_ANIMATIONS), renderer.getDefaultAsset(LookAssetTypes.GECKOLIB_ANIMATIONS));
    }

    private static @Nullable Animation findAnimation(String name, @Nullable BakedAnimations animations, @Nullable BakedAnimations fallback) {
        if (animations != null) {
            Animation animation = animations.getAnimation(name);
            if (animation != null)
                return animation;
        }
        if (fallback != null)
            return fallback.getAnimation(name);
        return null;
    }
}
