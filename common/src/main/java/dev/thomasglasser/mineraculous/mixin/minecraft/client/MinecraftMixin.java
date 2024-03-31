package dev.thomasglasser.mineraculous.mixin.minecraft.client;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Inject(method = "handleKeybinds", at = @At("TAIL"))
	private void handleKeybinds(CallbackInfo ci)
	{
		if (Minecraft.getInstance().gameRenderer.currentEffect() == null)
		{
			if (((DataHolder) ClientUtils.getMainClientPlayer()).getPersistentData().getBoolean(MineraculousEntityEvents.TAG_HASCATVISION))
			{
				MineraculousClientUtils.setShader(MineraculousEntityEvents.CAT_VISION_SHADER);
			}
			else
			{
				MineraculousClientUtils.setShader(null);
			}
		}
	}
}
