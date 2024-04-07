package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
	@Shadow public abstract long getLootTableSeed();

	private final LivingEntity INSTANCE = (LivingEntity) (Object) this;

	protected LivingEntityMixin(EntityType<?> entityType, Level level)
	{
		super(entityType, level);
	}

	@ModifyReturnValue(method = "isBlocking", at = @At("RETURN"))
	private boolean isBlocking(boolean original)
	{
		return INSTANCE.getUseItem().getItem() == MineraculousItems.CAT_STAFF.get() || original;
	}

	@Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci, ResourceLocation resourceLocation, LootTable loottable, LootParams.Builder lootparams$builder, LootParams lootParams)
	{
		if (TommyLibServices.ENTITY.getPersistentData(INSTANCE).getBoolean(MineraculousEntityEvents.TAG_CATACLYSMED))
		{
			loottable.getRandomItems(lootParams, getLootTableSeed(), stack -> INSTANCE.spawnAtLocation(MineraculousEntityEvents.convertToCataclysmDust(stack)));
			ci.cancel();
		}
	}
}
