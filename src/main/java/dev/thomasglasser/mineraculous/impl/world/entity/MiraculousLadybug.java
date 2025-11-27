package dev.thomasglasser.mineraculous.impl.world.entity;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends Entity {
    private MineraculousMathUtils.CatmullRom path = null;
    private float oldSplinePosition = 0;
    private float distanceNearestBlockTarget = 0;

    private static final EntityDataAccessor<MiraculousLadybugTargetData> DATA_TARGET = SynchedEntityData.defineId(MiraculousLadybug.class, MineraculousEntityDataSerializers.MIRACULOUS_LADYBUG_TARGET_DATA.get());
    private static final EntityDataAccessor<Float> DATA_SPLINE_POSITION = SynchedEntityData.defineId(MiraculousLadybug.class, EntityDataSerializers.FLOAT);

    public MiraculousLadybug(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public MiraculousLadybug(Level level) {
        this(MineraculousEntityTypes.MIRACULOUS_LADYBUG.get(), level);
    }

    public void setTargetData(MiraculousLadybugTargetData targetData) {
        if (!Objects.equals(targetData, getTargetData())) {
            entityData.set(DATA_TARGET, targetData);
        }
    }

    public MiraculousLadybugTargetData getTargetData() {
        return entityData.get(DATA_TARGET);
    }

    public void setSplinePosition(float splinePosition) {
        entityData.set(DATA_SPLINE_POSITION, splinePosition);
    }

    public float getSplinePosition() {
        return entityData.get(DATA_SPLINE_POSITION);
    }

    public float getOldSplinePosition() {
        return oldSplinePosition;
    }

    public MineraculousMathUtils.CatmullRom getPath() {
        return path;
    }

    public float getDistanceToNearestBlockTarget() {
        return distanceNearestBlockTarget;
    }

    public void setOldSplinePosition(float position) {
        oldSplinePosition = position;
    }

    public boolean shouldRender() {
        return path != null &&
                getSplinePosition() >= path.getFirstParameter() &&
                getSplinePosition() < path.getLastParameter() - 0.1d;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, new MiraculousLadybugTargetData());
        builder.define(DATA_SPLINE_POSITION, 0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setTargetData(MiraculousLadybugTargetData.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag.get("TargetData")).getOrThrow());
        setSplinePosition(compoundTag.getFloat("SplinePosition"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("TargetData", MiraculousLadybugTargetData.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), getTargetData()).getOrThrow());
        compoundTag.putFloat("SplinePosition", getSplinePosition());
    }

    @Override
    public void tick() {
        super.tick();
        MiraculousLadybugTargetData targetData = getTargetData();
        if (targetData.controlPoints() != null && !targetData.controlPoints().isEmpty()) {
            if (this.path == null) {
                setupPath();
            } else {
                setPosition();
                setFacingDirection();
                revertReachedTarget();
                spawnParticles();
                setDistanceNearestBlockTarget();
            }
        } else if (!level().isClientSide()) discard();

        setOldSplinePosition(getSplinePosition());
        tickData();
    }

    public void revertAllTargets(ServerLevel serverLevel) {
        Multimap<Integer, MiraculousLadybugTarget<?>> targetMap = this.getTargetData().targets();
        for (int index : targetMap.keySet()) {
            revertTargetsAtIndex(serverLevel, targetMap, index, true);
        }
    }

    private void tickData() {
        if (level() instanceof ServerLevel serverLevel) {
            MiraculousLadybugTargetData targetData = getTargetData();
            MiraculousLadybugTargetData newTargetData = targetData.tick(serverLevel);
            if (targetData != newTargetData) {
                setTargetData(newTargetData);
            }
        }
    }

    private void setupPath() {
        MiraculousLadybugTargetData targetData = this.getTargetData();
        path = new MineraculousMathUtils.CatmullRom(targetData.controlPoints());
        setSplinePosition((float) path.getFirstParameter());
    }

    private void setPosition() {
        if (level() instanceof ServerLevel) {
            float speed = MineraculousServerConfig.get().miraculousLadybugSpeed.get() / 100f;
            float afterMovement = (float) path.advanceParameter(getSplinePosition(), speed);
            if (shouldDiscard(afterMovement)) {
                discard();
            }
            this.moveTo(path.getPoint(afterMovement));
            setSplinePosition(afterMovement);
        }
    }

    private boolean shouldDiscard(float afterMovementPosition) {
        MiraculousLadybugTargetData data = getTargetData();
        Multimap<Integer, MiraculousLadybugTarget<?>> map = data.targets();
        for (MiraculousLadybugTarget<?> target : map.values()) {
            if (target.isReverting()) {
                return false;
            }
        }
        return getSplinePosition() == afterMovementPosition;
    }

    private void setFacingDirection() {
        double splinePositionParameter = getSplinePosition();
        Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
        double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
        double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
    }

    private void revertReachedTarget() {
        MiraculousLadybugTargetData targetData = getTargetData();
        Multimap<Integer, MiraculousLadybugTarget<?>> targetMap = targetData.targets();
        float splinePos = getSplinePosition();
        if (level() instanceof ServerLevel serverLevel) {
            int approaching = path.findSegment(splinePos);
            if (approaching != -1) {
                int passed = approaching - 2; //subtracted the first ghost point
                if (targetMap.containsKey(passed)) {
                    revertTargets(serverLevel, targetMap, passed);
                }
            }
        }
    }

    private void revertTargets(ServerLevel serverLevel, Multimap<Integer, MiraculousLadybugTarget<?>> targetMap, int index) {
        revertTargetsAtIndex(serverLevel, targetMap, index, false);
    }

    private void revertTargetsAtIndex(ServerLevel serverLevel, Multimap<Integer, MiraculousLadybugTarget<?>> targetMap, int index, boolean instant) {
        MiraculousLadybugTargetData targetData = getTargetData();
        Multimap<Integer, MiraculousLadybugTarget<?>> newTargets = LinkedHashMultimap.create(targetMap);

        boolean changed = false;

        for (MiraculousLadybugTarget<?> target : targetMap.get(index)) {
            if (!target.isReverting()) {
                if (instant)
                    target.revert(serverLevel, true);
                else {
                    MiraculousLadybugTarget<?> newTarget = target.revert(serverLevel, false);
                    if (newTarget != target) {
                        newTargets.remove(index, target);
                        changed = true;
                        if (newTarget != null)
                            newTargets.put(index, newTarget);
                    }
                }
            }
        }

        if (changed) {
            MiraculousLadybugTargetData newTargetData = targetData.withTargets(newTargets);
            this.setTargetData(newTargetData);
        }
    }

    private void spawnParticles() {
        if (level().isClientSide() && shouldRender()) {
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

    private void setDistanceNearestBlockTarget() {
        if (level().isClientSide()) {
            this.distanceNearestBlockTarget = findNearestTargetDistance(position());
        }
    }

    private float findNearestTargetDistance(Vec3 pos) {
        MiraculousLadybugTargetData data = this.getTargetData();
        Multimap<Integer, MiraculousLadybugTarget<?>> targets = data.targets();

        int approaching = path.findSegment(getSplinePosition());
        int passedControlPoint = approaching != -1 ? approaching - 3 : 0;

        MiraculousLadybugTarget<?> backTarget = scanAnyTarget(targets, passedControlPoint, false, (float) path.getFirstParameter());
        MiraculousLadybugTarget<?> frontTarget = scanAnyTarget(targets, passedControlPoint + 1, true, (float) path.getLastParameter());

        if (frontTarget == null && backTarget == null) {
            return Float.MAX_VALUE;
        }

        float backDistance = backTarget != null ? (float) pos.distanceTo(backTarget.position()) : Float.MAX_VALUE;
        float frontDistance = frontTarget != null ? (float) pos.distanceTo(frontTarget.position()) : Float.MAX_VALUE;

        return Math.min(backDistance, frontDistance);
    }

    private MiraculousLadybugTarget<?> scanAnyTarget(
            Multimap<Integer, MiraculousLadybugTarget<?>> targets,
            int currentlyPassedControlPoint,
            boolean forward,
            float limit) {
        int step = forward ? 1 : -1;
        int i = currentlyPassedControlPoint;

        while ((step > 0 && i <= limit) || (step < 0 && i >= limit)) {
            for (MiraculousLadybugTarget<?> target : targets.get(i)) {
                if (target.shouldExpandMiraculousLadybug())
                    return target;
            }
            i += step;
        }
        return null;
    }
}
