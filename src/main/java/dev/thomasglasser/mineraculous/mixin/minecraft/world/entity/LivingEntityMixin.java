package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract long getLootTableSeed();

    @Unique
    private final LivingEntity mineraculous$INSTANCE = (LivingEntity) (Object) this;

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci, ResourceKey<LootTable> resourceKey, LootTable lootTable, LootParams.Builder builder, LootParams lootParams) {
        if (MineraculousEntityEvents.isCataclysmed(mineraculous$INSTANCE)) {
            lootTable.getRandomItems(lootParams, getLootTableSeed(), stack -> mineraculous$INSTANCE.spawnAtLocation(MineraculousEntityEvents.convertToCataclysmDust(stack)));
            ci.cancel();
        }
    }
}
