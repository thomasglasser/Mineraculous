package dev.thomasglasser.mineraculous.impl.world.item;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.model.GeoModel;

public record ItemAnimationData(boolean flying, boolean swimming, boolean sprinting, boolean walking, boolean idle) {
    public <T extends GeoAnimatable> ItemAnimationData(GeoModel<T> model, T animatable) {
        this(
                hasAnimation(DefaultAnimations.FLY, model, animatable),
                hasAnimation(DefaultAnimations.SWIM, model, animatable),
                hasAnimation(DefaultAnimations.RUN, model, animatable),
                hasAnimation(DefaultAnimations.WALK, model, animatable),
                hasAnimation(DefaultAnimations.IDLE, model, animatable));
    }

    private static <T extends GeoAnimatable> boolean hasAnimation(RawAnimation animation, GeoModel<T> model, T animatable) {
        for (RawAnimation.Stage stage : animation.getAnimationStages()) {
            if (model.getAnimation(animatable, stage.animationName()) != null) {
                return true;
            }
        }
        return false;
    }
}
