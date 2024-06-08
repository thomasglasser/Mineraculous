package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity.player;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class FabricPlayerMixin
{
	private final Player INSTANCE = (Player) (Object) this;

	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo info)
	{
		MineraculousEntityEvents.playerTick(INSTANCE);
	}
}
