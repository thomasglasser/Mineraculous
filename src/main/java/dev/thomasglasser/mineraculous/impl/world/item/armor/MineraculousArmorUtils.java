package dev.thomasglasser.mineraculous.impl.world.item.armor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;

public class MineraculousArmorUtils {
    private static final Map<? super GeoAnimatable, ArmorAnimationData> ANIMATION_DATA = new Object2ObjectOpenHashMap<>();

    public static void clearAnimationData() {
        ANIMATION_DATA.clear();
    }

    public static <T extends GeoAnimatable> PlayState genericOptionalArmorController(AnimationState<T> state, T animatable) {
        Entity entity = state.getData(DataTickets.ENTITY);
        if (entity != null) {
            boolean flying = entity instanceof Player player ? player.getAbilities().flying : entity instanceof FlyingAnimal flyingAnimal ? flyingAnimal.isFlying() : entity instanceof FlyingMob || entity.isFlapping();
            boolean swimming = entity.isSwimming();
            boolean sprinting = entity.isSprinting();
            boolean walking = state.isMoving();
            GeoModel<T> model = (GeoModel<T>) RenderUtil.getGeoModelForArmor(state.getData(DataTickets.ITEMSTACK));
            if (model != null) {
                ArmorAnimationData data = ANIMATION_DATA.computeIfAbsent(animatable, a -> new ArmorAnimationData(model, animatable));
                if (flying && data.flying())
                    return state.setAndContinue(DefaultAnimations.FLY);
                else if (swimming && data.swimming())
                    return state.setAndContinue(DefaultAnimations.SWIM);
                else if (sprinting && data.sprinting())
                    return state.setAndContinue(DefaultAnimations.RUN);
                else if (walking && data.walking())
                    return state.setAndContinue(DefaultAnimations.WALK);
                else if (data.idle())
                    return state.setAndContinue(DefaultAnimations.IDLE);
            }
        }
        return PlayState.STOP;
    }
}
