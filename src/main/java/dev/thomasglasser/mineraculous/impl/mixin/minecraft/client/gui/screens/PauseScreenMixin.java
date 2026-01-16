package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    private PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "TAIL"))
    private void addLookScreenButton(CallbackInfo ci, @Local GridLayout gridLayout) {
        addRenderableWidget(MineraculousClientUtils.createMiraculousLooksButton(this, gridLayout));
    }
}
