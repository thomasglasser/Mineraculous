package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MiraculousLadybug extends Entity {
    public MineraculousMathUtils.CatmullRom path;
    boolean shouldUpdatePath;
    public double splinePositionParameter;

    public MiraculousLadybug(EntityType<? extends MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.shouldUpdatePath = true;
        this.splinePositionParameter = 0;
    }

    @Override
    public void tick() {
        super.tick();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        if ((targetData.blockTargets() == null || targetData.blockTargets().isEmpty())) {
            this.discard();
            return;
        }
        List<BlockPos> blockTargets = MineraculousMathUtils.sortBlockTargets(targetData.blockTargets(), this.blockPosition());
        if (this.shouldUpdatePath) { // this part should run only once in the entity's lifetime
            ArrayList<Vec3> targets = new ArrayList<>();
            for (BlockPos blockPos : blockTargets) {
                targets.add(blockPos.getCenter());
            }
            this.path = new MineraculousMathUtils.CatmullRom(targets);
            this.splinePositionParameter = path.getFirstParameter();
            this.shouldUpdatePath = false;
        } else {
            splinePositionParameter = path.advanceParameter(splinePositionParameter, 0.8); //0.8
            this.setPos(path.getPoint(splinePositionParameter));

            Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
            double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
            double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
            this.setYRot((float) yaw);
            this.setXRot((float) pitch);
        }

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
