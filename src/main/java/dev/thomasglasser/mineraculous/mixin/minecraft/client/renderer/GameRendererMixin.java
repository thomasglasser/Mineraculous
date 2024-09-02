package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
    private Minecraft minecraft;

    @Inject(method = "togglePostEffect", at = @At("HEAD"), cancellable = true)
    private void togglePostEffect(CallbackInfo ci) {
        if (TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer()).getBoolean(MineraculousEntityEvents.TAG_HASNIGHTVISION)) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean renderLevel(boolean original) {
        if (minecraft.player.hasEffect(MineraculousMobEffects.CATACLYSMED)) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean render(boolean original) {
        if (minecraft.player.hasEffect(MineraculousMobEffects.CATACLYSMED)) {
            return true;
        }
        return original;
    }
}
