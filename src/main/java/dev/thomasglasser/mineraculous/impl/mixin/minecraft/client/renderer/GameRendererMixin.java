package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
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
        if (player != null) {
            player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).shader().ifPresent(MineraculousClientUtils::setShader);
        }
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean checkCatacylsmedForConfusionSpin(boolean original) {
        Player player = this.minecraft.player;
        return original || (player != null && player.hasEffect(MineraculousMobEffects.CATACLYSM));
    }
}
