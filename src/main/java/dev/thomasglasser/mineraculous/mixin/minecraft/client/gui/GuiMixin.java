package dev.thomasglasser.mineraculous.mixin.minecraft.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyExpressionValue(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean renderCameraOverlays(boolean original) {
        if (Minecraft.getInstance().player.hasEffect(MineraculousMobEffects.CATACLYSMED)) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean renderCrosshair(boolean original) {
        if (Minecraft.getInstance().screen instanceof RadialMenuScreen) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "isExperienceBarVisible", at = @At("RETURN"))
    private boolean isExperienceBarVisible(boolean original) {
        if (MineraculousClientUtils.isCameraEntityOther())
            return false;
        return original;
    }
}
