package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
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
    public double oldSplinePosition;
    private double distanceNearestTarget = 0;

    public MiraculousLadybug(EntityType<? extends MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.shouldUpdatePath = true;
        this.splinePositionParameter = 0;
        this.oldSplinePosition = 0;
    }

    public MiraculousLadybug(Level level) {
        super(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), level);
        this.noPhysics = true;
        this.noCulling = true;
        this.shouldUpdatePath = true;
        this.splinePositionParameter = 0;
        this.oldSplinePosition = 0;
    }

    @Override
    public void tick() {
        super.tick();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        if ((targetData.blockTargets() == null || targetData.blockTargets().isEmpty())) {
            this.discard();
            return;
        }
        if (this.shouldUpdatePath) { // this part should run only once in the entity's lifetime
            //TODO extract methods for target setup and make a list with universal targets
            List<BlockPos> unsortedBlockTargets = targetData.blockTargets();
            List<Vec3> blockTargets = MineraculousMathUtils.sortTargets(
                    MineraculousMathUtils.getCenter(unsortedBlockTargets.subList(1, unsortedBlockTargets.size())));
            blockTargets.addFirst(unsortedBlockTargets.getFirst().getCenter());
            updatePath(blockTargets);
            MineraculousConstants.LOGGER.info("" + blockTargets);
        } else {
            setPosition();
            setFacingDirection();
            if (level().isClientSide) {
                setDistanceNearestTarget();
            }
        }
        renderParticles();
    }

    private void setDistanceNearestTarget() {
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        double distance = Double.MAX_VALUE;
        for (BlockPos blockPos : targetData.blockTargets()) {
            distance = Math.min(distance, blockPos.getCenter().distanceTo(this.position()));
        }
        this.distanceNearestTarget = distance;
    }

    private void renderParticles() {
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

    private void setPosition() {
        double before = splinePositionParameter;
        splinePositionParameter = path.advanceParameter(splinePositionParameter, 0.8); //0.8
        if (before == splinePositionParameter) this.discard();
        this.setPos(path.getPoint(splinePositionParameter));
    }

    private void setFacingDirection() {
        Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
        double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
        double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
    }

    private void updatePath(List<Vec3> blockTargets) {
        if (blockTargets.size() >= 2) {
            ArrayList<Vec3> targets = new ArrayList<>(blockTargets.subList(2, blockTargets.size()));
            Vec3 spawnPos = blockTargets.get(0);
            Vec3 circlePos = blockTargets.get(1);
            for (int i = 0; i <= 25; i++) {
                targets.add(i, spawnPos.lerp(circlePos, i / 25d));
            }
            this.path = new MineraculousMathUtils.CatmullRom(targets);
            this.splinePositionParameter = path.getFirstParameter();
            this.shouldUpdatePath = false;
        } else this.discard();
    }

    public double getDistanceToNearestTarget() {
        return this.distanceNearestTarget;
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
