package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	@Inject(method = "die", at = @At("HEAD"))
	private void die(DamageSource damageSource, CallbackInfo ci)
	{
		MineraculousEntityEvents.onDeath((LivingEntity) (Object) this);
	}
}
