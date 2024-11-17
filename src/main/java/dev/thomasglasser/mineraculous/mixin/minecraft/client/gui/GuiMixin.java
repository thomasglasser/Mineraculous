package dev.thomasglasser.mineraculous.mixin.minecraft.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyExpressionValue(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean renderCameraOverlays(boolean original) {
        if (ClientUtils.getMainClientPlayer().hasEffect(MineraculousMobEffects.CATACLYSMED)) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean renderCrosshair(boolean original) {
        if (Minecraft.getInstance().screen instanceof RadialMenuScreen || (TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer()).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && MineraculousClientUtils.isCameraEntityOther())) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "isExperienceBarVisible", at = @At("RETURN"))
    private boolean isExperienceBarVisible(boolean original) {
        if (TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer()).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && MineraculousClientUtils.isCameraEntityOther())
            return false;
        return original;
    }

    @ModifyReturnValue(method = "getCameraPlayer", at = @At("TAIL"))
    private Player getCameraPlayer(Player original) {
        if (original != null && TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer()).getBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK) && MineraculousClientUtils.isCameraEntityOther())
            return null;
        return original;
    }
}
