package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	@Inject(method = "togglePostEffect", at = @At("HEAD"), cancellable = true)
	private void togglePostEffect(CallbackInfo ci)
	{
		if (((DataHolder) ClientUtils.getMainClientPlayer()).getPersistentData().getBoolean(MineraculousEntityEvents.TAG_HASCATVISION))
		{
			ci.cancel();
		}
	}
}
