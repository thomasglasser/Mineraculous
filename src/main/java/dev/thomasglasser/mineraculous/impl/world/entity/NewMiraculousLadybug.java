package dev.thomasglasser.mineraculous.impl.world.entity;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.NewMLBBlockClusterTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.NewMLBBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.NewMLBTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.NewMLBTargetData;
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

public class NewMiraculousLadybug extends Entity {
    private MineraculousMathUtils.CatmullRom path = null;
    private float oldSplinePosition = 0;
    private float distanceNearestBlockTarget = 0;

    private static final EntityDataAccessor<NewMLBTargetData> DATA_TARGET = SynchedEntityData.defineId(NewMiraculousLadybug.class, MineraculousEntityDataSerializers.NEW_MIRACULOUS_LADYBUG_TARGET_DATA.get());
    private static final EntityDataAccessor<Float> DATA_SPLINE_POSITION = SynchedEntityData.defineId(NewMiraculousLadybug.class, EntityDataSerializers.FLOAT);

    public NewMiraculousLadybug(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public NewMiraculousLadybug(Level level) {
        this(MineraculousEntityTypes.NEW_MIRACULOUS_LADYBUG.get(), level);
    }

    public void setTargetData(NewMLBTargetData targetData) {
        entityData.set(DATA_TARGET, targetData);
    }

    public NewMLBTargetData getTargetData() {
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

    public void setOldSplinePosition(float f) {
        oldSplinePosition = f;
    }

    public boolean shouldRender() {
        return path != null &&
                getSplinePosition() >= path.getFirstParameter() &&
                getSplinePosition() < path.getLastParameter() - 0.1d;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET, new NewMLBTargetData());
        builder.define(DATA_SPLINE_POSITION, 0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setTargetData(NewMLBTargetData.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag.get("TargetData")).getOrThrow());
        setSplinePosition(compoundTag.getFloat("SplinePosition"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("TargetData", NewMLBTargetData.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), getTargetData()).getOrThrow());
        compoundTag.putFloat("SplinePosition", getSplinePosition());
    }

    @Override
    public void tick() {
        super.tick();
        NewMLBTargetData targetData = getTargetData();
        if (targetData.controlPoints() != null && !targetData.controlPoints().isEmpty()) {
            if (this.path == null) {
                setupPath();
            } else {
                setPosition();
                setFacingDirection();
                spawnParticles();
                setDistanceNearestBlockTarget();
            }
        } else if (!level().isClientSide()) this.discard();
    }

    private void setupPath() {
        NewMLBTargetData targetData = this.getTargetData();
        path = new MineraculousMathUtils.CatmullRom(targetData.controlPoints());
        setSplinePosition((float) path.getFirstParameter());
    }

    private void setPosition() {
        if (level() instanceof ServerLevel) {
            double speed = MineraculousServerConfig.get().miraculousLadybugSpeed.get() / 100f;
            double splinePositionParameter = getSplinePosition();
            splinePositionParameter = path.advanceParameter(splinePositionParameter, speed);
            this.moveTo(path.getPoint(splinePositionParameter));
            setSplinePosition((float) splinePositionParameter);
        }
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
        NewMLBTargetData targetData = getTargetData();
        Multimap<Integer, NewMLBTarget> targetMap = targetData.targets();
        float splinePos = getSplinePosition();
        if (level() instanceof ServerLevel serverLevel) {
            int approaching = path.findSegment(splinePos);
            if (approaching != -1) {
                int passed = approaching - 3; //subtracted the first ghost point
                if (targetMap.containsKey(passed)) {
                    revertTargets(serverLevel, targetMap, passed);
                }
            }
        }
    }

    private void revertTargets(ServerLevel serverLevel, Multimap<Integer, NewMLBTarget> targetMap, int index) {
        revertTargetsAtIndex(serverLevel, targetMap, index, false);
    }

    private void revertTargetsAtIndex(ServerLevel serverLevel, Multimap<Integer, NewMLBTarget> targetMap, int index, boolean instant) {
        NewMLBTargetData targetData = getTargetData();
        Multimap<Integer, NewMLBTarget> newTargets = LinkedHashMultimap.create(targetMap);

        boolean changed = false;

        for (NewMLBTarget target : targetMap.get(index)) {
            if (!target.isReverting()) {
                if (instant)
                    target.instantRevert(serverLevel);
                else {
                    NewMLBTarget newTarget = target.startReversion(serverLevel);
                    newTargets.remove(index, target);
                    newTargets.put(index, newTarget);
                    changed = true;
                }
            }
        }

        /*if (changed) {
            NewMLBTarget updatedData = new NewMLBTarget(
                    targetData.pathControlPoints(),
                    newTargets,
                    targetData.splinePosition());
            this.setTargetData(updatedData);
        }*/
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
        NewMLBTargetData data = this.getTargetData();
        Multimap<Integer, NewMLBTarget> targets = data.targets();

        int approaching = path.findSegment(getSplinePosition());
        int passedControlPoint = approaching != -1 ? approaching - 3 : 0;

        NewMLBTarget backTarget = scanAnyTarget(targets, passedControlPoint, false, (float) path.getFirstParameter());
        NewMLBTarget frontTarget = scanAnyTarget(targets, passedControlPoint + 1, true, (float) path.getLastParameter());

        if (frontTarget == null && backTarget == null) {
            return Float.MAX_VALUE;
        }

        float backDistance = backTarget != null ? (float) pos.distanceTo(backTarget.getPosition()) : Float.MAX_VALUE;
        float frontDistance = frontTarget != null ? (float) pos.distanceTo(frontTarget.getPosition()) : Float.MAX_VALUE;

        return Math.min(backDistance, frontDistance);
    }

    private NewMLBTarget scanAnyTarget(
            Multimap<Integer, NewMLBTarget> targets,
            int currentlyPassedControlPoint,
            boolean forward,
            float limit) {
        int step = forward ? 1 : -1;
        int i = currentlyPassedControlPoint;

        while ((step > 0 && i <= limit) || (step < 0 && i >= limit)) {
            for (NewMLBTarget target : targets.get(i)) {
                // TODO when making the registry for targets ensure Target has a method which returns a boolean for this if
                if (target instanceof NewMLBBlockTarget || target instanceof NewMLBBlockClusterTarget) {
                    return target;
                }
            }
            i += step;
        }
        return null;
    }
}
