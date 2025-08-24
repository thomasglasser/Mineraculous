package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends PathfinderMob {
    private Vec3 target = Vec3.ZERO;

    public MiraculousLadybug(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public void setTarget(Vec3 target) {
        this.target = new Vec3(target.x, target.y, target.z);
    }

    public Vec3 getPosTarget() {
        return this.target;
    }

    @Override
    public void tick() {
        super.tick();
        /*Vec3 min = this.getPosition(0).add(-64, -64, -64);
        Vec3 max = this.getPosition(0).add(64, 64, 64);
        AABB field = new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
        List<Entity> nearby = this.level().getEntities(this, field, (Entity entity) -> !(entity instanceof MiraculousLadybug));
        this.target = this.level().getNearestEntity(nearby, TargetingConditions.DEFAULT, ));
        */
        //this.target = new Vec3(117, 78, 305);
        if (target != null) {
            Vec3 diff = target.subtract(this.position());
            if (diff.lengthSqr() > 0.01) {
                Vec3 dir = diff.normalize().scale(0.5);

                // move toward target
                this.setDeltaMovement(dir);

                double dx = dir.x;
                double dy = dir.y;
                double dz = dir.z;

                this.lookAt(EntityAnchorArgument.Anchor.EYES, this.target);
            }
        }

        Level level = this.level();
        if (level.isClientSide) {
            Vec3 look = this.getLookAngle().scale(-2);
            for (int i = 1; i <= 3; i++)
                level.addParticle(
                        MineraculousParticleTypes.STARLIGHT.get(),
                        this.getX() + look.x + Math.random() * 5 - 2.5,
                        this.getY() + look.y + Math.random() * 5 - 2.5,
                        this.getZ() + look.z + Math.random() * 5 - 2.5,
                        0, 0, 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.FLYING_SPEED, 0.1)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 1024);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        double x = compound.getDouble("TargetX");
        double y = compound.getDouble("TargetY");
        double z = compound.getDouble("TargetZ");
        setTarget(new Vec3(x, y, z));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putDouble("TargetX", getPosTarget().x);
        compound.putDouble("TargetY", getPosTarget().y);
        compound.putDouble("TargetZ", getPosTarget().z);
    }

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
