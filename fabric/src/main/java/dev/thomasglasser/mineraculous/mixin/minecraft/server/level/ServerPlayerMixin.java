package dev.thomasglasser.mineraculous.mixin.minecraft.server.level;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin
{
	@Inject(method = "die", at = @At("HEAD"))
	private void die(DamageSource damageSource, CallbackInfo ci)
	{
		MineraculousEntityEvents.onDeath((ServerPlayer)(Object)this);
	}
}
