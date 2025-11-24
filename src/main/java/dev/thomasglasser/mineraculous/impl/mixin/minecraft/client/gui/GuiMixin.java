package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean disablePortalOverlayWhenCataclysmed(boolean original) {
        LocalPlayer player = this.minecraft.player;
        return original || player != null && player.hasEffect(MineraculousMobEffects.CATACLYSM);
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean disableCrosshairForRadialMenuScreen(boolean original) {
        return original && !(this.minecraft.screen instanceof RadialMenuScreen);
    }
}
