package dev.thomasglasser.mineraculous.api.client.model;

import dev.thomasglasser.mineraculous.api.client.look.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class LookGeoModel<T extends GeoAnimatable> extends DefaultedItemGeoModel<T> {
    private final LookRenderer renderer;
    private final ResourceLocation defaultTexture;

    private BakedGeoModel currentModel = null;

    public LookGeoModel(LookRenderer renderer, ResourceLocation defaultSubpath, ResourceLocation defaultTexture) {
        super(defaultSubpath);
        this.renderer = renderer;
        this.defaultTexture = defaultTexture;
    }

    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel model = renderer.getAssetOrDefault(LookAssetTypes.GECKOLIB_MODEL, () -> super.getBakedModel(location));
        if (model != this.currentModel) {
            this.getAnimationProcessor().setActiveModel(model);
            this.currentModel = model;
        }
        return currentModel;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return renderer.getAssetOrDefault(LookAssetTypes.TEXTURE, () -> defaultTexture);
    }

    @Override
    public @Nullable Animation getAnimation(T animatable, String name) {
        BakedAnimations animations = renderer.getAsset(LookAssetTypes.GECKOLIB_ANIMATIONS);
        if (animations != null) {
            Animation animation = animations.getAnimation(name);
            if (animation != null)
                return animation;
        }

        try {
            return super.getAnimation(animatable, name);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
