package dev.thomasglasser.mineraculous.api.world.entity.projectile;

import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.tommylib.api.world.entity.projectile.ThrownSword;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class ItemBreakingQuicklyReturningThrownSword extends ThrownSword {
    protected ItemBreakingQuicklyReturningThrownSword(EntityType<? extends ItemBreakingQuicklyReturningThrownSword> entity, Level level) {
        super(entity, level);
    }

    protected ItemBreakingQuicklyReturningThrownSword(EntityType<? extends ItemBreakingQuicklyReturningThrownSword> entityType, LivingEntity shooter, Level level, ItemStack pickupItemStack, int baseDamage, @Nullable SoundEvent hitGroundSound, @Nullable SoundEvent returnSound) {
        super(entityType, shooter, level, pickupItemStack, baseDamage, hitGroundSound, returnSound);
    }

    protected ItemBreakingQuicklyReturningThrownSword(EntityType<? extends ItemBreakingQuicklyReturningThrownSword> entityType, double x, double y, double z, Level level, ItemStack pickupItemStack, int baseDamage, @Nullable SoundEvent hitGroundSound, @Nullable SoundEvent returnSound) {
        super(entityType, x, y, z, level, pickupItemStack, baseDamage, hitGroundSound, returnSound);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (entityHitResult.getEntity() instanceof ItemEntity itemEntity && level() instanceof ServerLevel serverLevel) {
            MineraculousEntityUtils.tryBreakItemEntity(entityHitResult, itemEntity, serverLevel, position());
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) || target.isAlive() && target instanceof ItemEntity;
    }

    @Override
    public byte getLoyalty() {
        return 10;
    }
}
