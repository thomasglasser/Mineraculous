package dev.thomasglasser.mineraculous.impl.world.entity;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends Entity {
    private MineraculousMathUtils.CatmullRom path = null;
    private double oldSplinePosition = 0;
    private double distanceNearestBlockTarget = 0;

    private static final EntityDataAccessor<MiraculousLadybugTargetData> DATA_TARGET = SynchedEntityData.defineId(MiraculousLadybug.class, MineraculousEntityDataSerializers.MIRACULOUS_LADYBUG_TARGET_DATA.get());

    public MiraculousLadybug(EntityType<? extends MiraculousLadybug> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public MiraculousLadybug(Level level) {
        this(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, new MiraculousLadybugTargetData());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        setTargetData(MiraculousLadybugTargetData.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compound.get("TargetData")).getOrThrow());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("TargetData", MiraculousLadybugTargetData.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), getTargetData()).getOrThrow());
    }

    public void setTargetData(MiraculousLadybugTargetData data) {
        entityData.set(DATA_TARGET, data);
    }

    public MiraculousLadybugTargetData getTargetData() {
        return entityData.get(DATA_TARGET);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        MiraculousLadybugTargetData targetData = this.getTargetData();
        if (targetData.pathControlPoints() != null && !targetData.pathControlPoints().isEmpty()) {
            if (this.path == null) {
                setupPath();
            } else {
                revertReachedTarget();
                setPosition(level);
                setFacingDirection();
                setDistanceNearestBlockTarget();
                spawnParticles(level);
            }
        } else this.discard();
    }

    public void revertAllTargets(ServerLevel serverLevel) {
        Multimap<Integer, MiraculousLadybugTarget> targetMap = this.getTargetData().targets();
        for (int index : targetMap.keySet()) {
            revertTargetsAtIndex(serverLevel, targetMap, index, true);
        }
    }

    public MineraculousMathUtils.CatmullRom getPath() {
        return path;
    }

    public void setOldSplinePosition(double d) {
        this.oldSplinePosition = d;
    }

    public double getOldSplinePosition() {
        return this.oldSplinePosition;
    }

    public double getDistanceToNearestBlockTarget() {
        return this.distanceNearestBlockTarget;
    }

    private void setupPath() {
        MiraculousLadybugTargetData targetData = this.getTargetData();
        this.path = new MineraculousMathUtils.CatmullRom(targetData.pathControlPoints());
        double splinePositionParameter = path.getFirstParameter();
        this.setTargetData(targetData.withSplinePosition(splinePositionParameter));
    }

    private void revertTarget(ServerLevel serverLevel, Multimap<Integer, MiraculousLadybugTarget> targetMap, int index) {
        revertTargetsAtIndex(serverLevel, targetMap, index, false);
    }

    private void revertTargetsAtIndex(ServerLevel serverLevel, Multimap<Integer, MiraculousLadybugTarget> targetMap, int index, boolean instant) {
        MiraculousLadybugTargetData targetData = this.getTargetData();
        Multimap<Integer, MiraculousLadybugTarget> newTargets = LinkedHashMultimap.create(targetMap);

        boolean changed = false;

        for (MiraculousLadybugTarget target : targetMap.get(index)) {
            if (target.shouldStartRevert()) {
                if (instant)
                    target.revertInstantly(serverLevel);
                else {
                    MiraculousLadybugTarget newTarget = target.revert(serverLevel);
                    newTargets.remove(index, target);
                    newTargets.put(index, newTarget);
                    changed = true;
                }
            }
        }

        if (changed) {
            MiraculousLadybugTargetData updatedData = new MiraculousLadybugTargetData(
                    targetData.pathControlPoints(),
                    newTargets,
                    targetData.splinePosition());
            this.setTargetData(updatedData);
        }
    }

    private void revertReachedTarget() {
        Level entityLevel = this.level();
        MiraculousLadybugTargetData targetData = this.getTargetData();
        Multimap<Integer, MiraculousLadybugTarget> targetMap = targetData.targets();
        double splinePos = targetData.splinePosition();
        if (entityLevel instanceof ServerLevel serverLevel) {
            int approaching = path.findSegment(splinePos);
            if (approaching != -1) {
                int passed = approaching - 3; //subtracted the first ghost point
                if (targetMap.containsKey(passed)) {
                    this.revertTarget(serverLevel, targetMap, passed);
                }
            }
        }
    }

    private void setDistanceNearestBlockTarget() {
        MiraculousLadybugTargetData targetData = this.getTargetData();
        Level level = this.level();
        Vec3 pos = this.position();
        double splinePos = targetData.splinePosition();
        if (level.isClientSide()) {
            NearestTargetResult<MiraculousLadybugBlockTarget> result = findNearestTarget(
                    splinePos,
                    pos,
                    MiraculousLadybugBlockTarget.class);

            this.distanceNearestBlockTarget = result.distance();
        }
    }

    private <T extends MiraculousLadybugTarget> NearestTargetResult<T> findNearestTarget(
            double splinePos,
            Vec3 pos,
            Class<T> type) {
        MiraculousLadybugTargetData data = this.getTargetData();
        Multimap<Integer, MiraculousLadybugTarget> map = data.targets();

        int approaching = path.findSegment(splinePos);
        int passedControlPoint = approaching != -1 ? approaching - 3 : 0;
        T back = scanTargets(map, type, passedControlPoint, false, path.getFirstParameter());
        T front = scanTargets(map, type, passedControlPoint + 1, true, path.getLastParameter());

        if (front == null && back == null)
            return new NearestTargetResult<>(null, 100);

        double backDist = back != null ? pos.distanceTo(back.position()) : Double.MAX_VALUE;
        double frontDist = front != null ? pos.distanceTo(front.position()) : Double.MAX_VALUE;

        return backDist < frontDist
                ? new NearestTargetResult<>(back, backDist)
                : new NearestTargetResult<>(front, frontDist);
    }

    private <T extends MiraculousLadybugTarget> T scanTargets(
            Multimap<Integer, MiraculousLadybugTarget> targets,
            Class<T> type, int currentlyPassedControlPoint, boolean forward, double limit) {
        int i = currentlyPassedControlPoint;
        int step = forward ? 1 : -1;
        while ((step > 0 && i <= limit) || (step < 0 && i >= limit)) {
            for (MiraculousLadybugTarget t : targets.get(i)) {
                if (type.isInstance(t)) return type.cast(t);
            }
            i += step;
        }
        return null;
    }

    private void spawnParticles(Level level) {
        if (level.isClientSide()) {
            Vec3 look = this.getLookAngle().scale(-2);
            double bx = this.getX() + look.x;
            double by = this.getY() + look.y;
            double bz = this.getZ() + look.z;

            for (int i = 0; i < 5; i++) {
                level().addParticle(
                        MineraculousParticleTypes.SPARKLE.get(),
                        bx + randomOffset(), by + randomOffset(), bz + randomOffset(),
                        0, 0, 0);
            }
            for (int i = 0; i < 3; i++) {
                level().addParticle(
                        MineraculousParticleTypes.SUMMONING_LADYBUG.get(),
                        bx + randomOffset(), by + randomOffset(), bz + randomOffset(),
                        0, 0, 0);
            }
        }
    }

    private static double randomOffset() {
        return Math.random() * 5 - 2.5;
    }

    private boolean shouldDiscard(double previousSplinePos, double splinePos, MiraculousLadybugTargetData data) {
        if (previousSplinePos != splinePos) {
            return false;
        }

        Multimap<Integer, MiraculousLadybugTarget> map = data.targets();
        for (MiraculousLadybugTarget target : map.values()) {
            if (!target.shouldStartRevert()) {
                return false;
            }
        }

        return true;
    }

    private void setPosition(Level level) {
        if (level instanceof ServerLevel) {
            double speed = MineraculousServerConfig.get().miraculousLadybugSpeed.get() / 100f;
            MiraculousLadybugTargetData targetData = this.getTargetData();
            double splinePositionParameter = targetData.splinePosition();
            double before = splinePositionParameter;
            splinePositionParameter = path.advanceParameter(splinePositionParameter, speed);
            if (this.shouldDiscard(before, splinePositionParameter, targetData)) {
                this.discard();
            }
            this.moveTo(path.getPoint(splinePositionParameter));
            this.setTargetData(targetData.withSplinePosition(splinePositionParameter));
        }
    }

    private void setFacingDirection() {
        MiraculousLadybugTargetData targetData = this.getTargetData();
        double splinePositionParameter = targetData.splinePosition();
        Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
        double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
        double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
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

    private record NearestTargetResult<T>(T target, double distance) {}
}
