package dev.thomasglasser.mineraculous.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract long getLootTableSeed();

    @Unique
    private final LivingEntity mineraculous$instance = (LivingEntity) (Object) this;

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropFromLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"), cancellable = true)
    private void dropDustIfCataclysmed(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci, @Local LootTable lootTable, @Local LootParams lootParams) {
        if (mineraculous$instance.hasEffect(MineraculousMobEffects.CATACLYSM)) {
            lootTable.getRandomItems(lootParams, getLootTableSeed(), stack -> mineraculous$instance.spawnAtLocation(MineraculousEntityEvents.convertToCataclysmDust(stack)));
            ci.cancel();
        }
    }
}
