package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FabricLivingEntityMixin
{
	@Unique
	LivingEntity INSTANCE = (LivingEntity) (Object) this;

	@Inject(method = "die", at = @At("HEAD"))
	private void die(DamageSource damageSource, CallbackInfo ci)
	{
		MineraculousEntityEvents.onDeath(INSTANCE);
	}

	@Inject(method = "hurt", at = @At("HEAD"))
	private void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		MineraculousEntityEvents.onLivingAttack(INSTANCE, source);
	}

	@Inject(method = "removeAllEffects", at = @At("HEAD"), cancellable = true)
	private void removeAllEffects(CallbackInfoReturnable<Boolean> cir)
	{
		if (MineraculousEntityEvents.isCataclysmed(INSTANCE))
		{
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "heal", at = @At("HEAD"), cancellable = true)
	private void heal(float healAmount, CallbackInfo ci)
	{
		if (MineraculousEntityEvents.isCataclysmed(INSTANCE))
		{
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci)
	{
		MineraculousEntityEvents.tick(INSTANCE);
	}
}
