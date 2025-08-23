package dev.thomasglasser.mineraculous.impl.world.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends Entity {
    private Vec3 target = Vec3.ZERO;

    public MiraculousLadybug(EntityType<MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public void setTarget(Vec3 target) {
        this.target = target;
    }

    public Vec3 getTarget() {
        return this.target == null ? Vec3.ZERO : this.target;
    }

    @Override
    public void tick() {
        super.tick();

        if (target != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, target);
            this.setDeltaMovement(this.getLookAngle().scale(0.1));
        }
    }

    public void lookAt(Vec3 target) {
        Vec3 eyes = this.position().add(0, this.getEyeHeight(), 0); // from eye position
        Vec3 diff = target.subtract(eyes);

        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;

        // Horizontal angle (yaw)
        float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90F;

        // Vertical angle (pitch)
        float pitch = (float) (-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180F / Math.PI)));

        this.setYRot(yaw);
        this.setXRot(pitch);

        // Also update old rotations so it doesn’t snap back
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        double x = compound.getDouble("TargetX");
        double y = compound.getDouble("TargetY");
        double z = compound.getDouble("TargetZ");
        setTarget(new Vec3(x, y, z));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putDouble("TargetX", getTarget().x);
        compound.putDouble("TargetY", getTarget().y);
        compound.putDouble("TargetZ", getTarget().z);
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
