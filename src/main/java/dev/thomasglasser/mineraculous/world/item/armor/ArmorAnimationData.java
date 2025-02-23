package dev.thomasglasser.mineraculous.world.item.armor;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.model.GeoModel;

public record ArmorAnimationData(boolean flying, boolean swimming, boolean sprinting, boolean walking, boolean idle) {
    public <T extends GeoAnimatable> ArmorAnimationData(GeoModel<T> model, T animatable) {
        this(
                DefaultAnimations.FLY.getAnimationStages().stream().anyMatch(stage -> model.getAnimation(animatable, stage.animationName()) != null),
                DefaultAnimations.SWIM.getAnimationStages().stream().anyMatch(stage -> model.getAnimation(animatable, stage.animationName()) != null),
                DefaultAnimations.RUN.getAnimationStages().stream().anyMatch(stage -> model.getAnimation(animatable, stage.animationName()) != null),
                DefaultAnimations.WALK.getAnimationStages().stream().anyMatch(stage -> model.getAnimation(animatable, stage.animationName()) != null),
                DefaultAnimations.IDLE.getAnimationStages().stream().anyMatch(stage -> model.getAnimation(animatable, stage.animationName()) != null));
    }
}
