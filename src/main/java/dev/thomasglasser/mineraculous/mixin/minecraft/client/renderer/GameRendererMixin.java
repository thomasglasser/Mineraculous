package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "togglePostEffect", at = @At("TAIL"))
    private void resetPostEffectToggleIfSpecial(CallbackInfo ci) {
        Player player = this.minecraft.player;
        // TODO: Check in kamiko view
        if (player != null) {
            AbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            abilityEffectData.nightVisionShader().ifPresent(MineraculousClientUtils::setShader);
        }
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean checkCatacylsmedForConfusionSpin(boolean original) {
        Player player = this.minecraft.player;
        return original || (player != null && player.hasEffect(MineraculousMobEffects.CATACLYSMED));
    }
}
