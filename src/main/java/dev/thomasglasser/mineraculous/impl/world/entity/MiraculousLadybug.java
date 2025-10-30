package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
//TODO clean this code
public class MiraculousLadybug extends Entity {
    public MineraculousMathUtils.CatmullRom path = null;
    public double oldSplinePosition = 0;
    private double distanceNearestBlockTarget = 0;

    public MiraculousLadybug(EntityType<? extends MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public MiraculousLadybug(Level level) {
        super(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        double splinePositionParameter = targetData.splinePosition();
        if (targetData.pathControlPoints() != null && !targetData.pathControlPoints().isEmpty()) {
            if (this.path == null) {
                this.path = new MineraculousMathUtils.CatmullRom(targetData.pathControlPoints());
                splinePositionParameter = path.getFirstParameter();
            } else {
                splinePositionParameter = setPosition(splinePositionParameter);
                setFacingDirection(splinePositionParameter);
                setDistanceNearestBlockTarget();
                revertReachedTarget();
            }
            if (!level.isClientSide()) {
                targetData.withSplinePosition(splinePositionParameter).save(this, true);
            } else {
                renderParticles();
            }
        } else this.discard();
    }

    private void revertReachedTarget() {
        Level entityLevel = this.level();
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        var targetMap = targetData.targets();
        double splinePos = targetData.splinePosition();
        if (entityLevel instanceof ServerLevel serverLevel) {
            int approaching = path.findSegment(splinePos);
            if (approaching != -1) {
                int passed = approaching - 3; //subtracted the first ghost point
                if (targetMap.containsKey(passed)) {
                    for (MiraculousLadybugTargetData.Target target : targetMap.get(passed)) {
                        target.revert(serverLevel);
                    }
                }
            }
        }
    }

    private void setDistanceNearestBlockTarget() {
        MiraculousLadybugTargetData targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        Level level = this.level();
        Vec3 pos = this.position();
        double splinePos = targetData.splinePosition();
        if (level.isClientSide()) {
            NearestTargetResult<MiraculousLadybugTargetData.BlockTarget> result = findNearestTargetData(
                    splinePos,
                    pos,
                    MiraculousLadybugTargetData.BlockTarget.class);

            this.distanceNearestBlockTarget = result.distance();
        }
    }

    private <T extends MiraculousLadybugTargetData.Target> T findNearestTarget(Class<T> type, int start, int step, double limit) {
        var targetData = this.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        var targetMap = targetData.targets();

        int i = start;
        while ((step > 0 && i <= limit) || (step < 0 && i >= limit)) {
            for (var target : targetMap.get(i)) {
                if (type.isInstance(target)) {
                    return type.cast(target);
                }
            }
            i += step;
        }
        return null;
    }

    private void renderParticles() {
        Level level = this.level();
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

    private double setPosition(double splinePositionParameter) {
        double before = splinePositionParameter;
        splinePositionParameter = path.advanceParameter(splinePositionParameter, 0.7); //TODO make the speed server configurable 0.6 -> 0.8 (def: 0.7)
        if (before == splinePositionParameter) this.discard();
        this.setPos(path.getPoint(splinePositionParameter));
        return splinePositionParameter;
    }

    private void setFacingDirection(double splinePositionParameter) {
        Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
        double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
        double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
    }

    public double getDistanceToNearestBlockTarget() {
        return this.distanceNearestBlockTarget;
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

    private <T extends MiraculousLadybugTargetData.Target> NearestTargetResult<T> findNearestTargetData(
            double splinePos,
            Vec3 pos,
            Class<T> targetType) {
        T back = findNearestTarget(targetType, (int) splinePos, -1, path.getFirstParameter());
        T front = findNearestTarget(targetType, (int) splinePos + 1, 1, path.getLastParameter());

        if (front == null && back == null) {
            return new NearestTargetResult<>(null, 100);
        } else if (front == null) {
            return new NearestTargetResult<>(back, pos.distanceTo(back.position()));
        } else if (back == null) {
            return new NearestTargetResult<>(front, pos.distanceTo(front.position()));
        } else {
            double distBack = pos.distanceTo(back.position());
            double distFront = pos.distanceTo(front.position());
            return distBack < distFront
                    ? new NearestTargetResult<>(back, distBack)
                    : new NearestTargetResult<>(front, distFront);
        }
    }

    private record NearestTargetResult<T>(T target, double distance) {}
}
