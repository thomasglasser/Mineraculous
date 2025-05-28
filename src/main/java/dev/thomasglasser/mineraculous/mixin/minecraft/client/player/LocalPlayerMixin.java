package dev.thomasglasser.mineraculous.mixin.minecraft.client.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Unique
    private final LocalPlayer mineraculous$instance = (LocalPlayer) (Object) this;

    @ModifyExpressionValue(method = "handleConfusionTransitionEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean checkCatacylsmedForConfusionSpin(boolean original) {
        return original || mineraculous$instance.hasEffect(MineraculousMobEffects.CATACLYSMED);
    }

    @ModifyExpressionValue(method = "handleConfusionTransitionEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance getCatacylsmForConfusionSpin(MobEffectInstance original) {
        if (original == null && mineraculous$instance.hasEffect(MineraculousMobEffects.CATACLYSMED)) {
            return mineraculous$instance.getEffect(MineraculousMobEffects.CATACLYSMED);
        }
        return original;
    }
}
