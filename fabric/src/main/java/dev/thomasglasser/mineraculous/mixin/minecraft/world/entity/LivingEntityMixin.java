package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
	@Unique
	LivingEntity INSTANCE = (LivingEntity) (Object) this;

	@Shadow public abstract long getLootTableSeed();

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

	@Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci, ResourceLocation resourceLocation, LootTable loottable, LootParams.Builder lootparams$builder, LootParams lootParams)
	{
		if (((DataHolder)(INSTANCE)).getPersistentData().getBoolean(MineraculousEntityEvents.TAG_CATACLYSMED))
		{
			loottable.getRandomItems(lootParams, getLootTableSeed(), stack -> INSTANCE.spawnAtLocation(MineraculousEntityEvents.convertToMiraculousDust(stack), 0.0F));
			ci.cancel();
		}
	}

	@Inject(method = "removeAllEffects", at = @At("HEAD"), cancellable = true)
	private void removeAllEffects(CallbackInfoReturnable<Boolean> cir)
	{
		if (MineraculousEntityEvents.isCataclysmed(INSTANCE))
		{
			cir.setReturnValue(false);
		}
	}
}
