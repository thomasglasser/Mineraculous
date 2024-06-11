package dev.thomasglasser.mineraculous.mixin.minecraft.client.gui;

import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class FabricGuiMixin
{
	@Final
	@Shadow
	private LayeredDraw layers;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Minecraft minecraft, CallbackInfo ci)
	{
		layers.add(MineraculousClientEvents::renderStealingProgressBar);
	}
}