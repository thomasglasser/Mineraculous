package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends Entity {
    boolean shouldUpdatePath;
    public double t; //TODO rename to something suggestive

    public MiraculousLadybug(EntityType<? extends MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.shouldUpdatePath = true;
        this.t = 0;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide) {
            Vec3 look = this.getLookAngle().scale(-2);
            for (int i = 1; i <= 5; i++)
                level.addParticle(
                        MineraculousParticleTypes.SPARKLE.get(),
                        this.getX() + look.x + Math.random() * 5 - 2.5,
                        this.getY() + look.y + Math.random() * 5 - 2.5,
                        this.getZ() + look.z + Math.random() * 5 - 2.5,
                        0, 0, 0);
            for (int i = 1; i <= 3; i++)
                level.addParticle(
                        MineraculousParticleTypes.SUMMONING_LADYBUG.get(),
                        this.getX() + look.x + Math.random() * 5 - 2.5,
                        this.getY() + look.y + Math.random() * 5 - 2.5,
                        this.getZ() + look.z + Math.random() * 5 - 2.5,
                        0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
