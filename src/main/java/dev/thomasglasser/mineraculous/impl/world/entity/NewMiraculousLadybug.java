package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
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
        Level level = this.level();
        NewMLBTargetData targetData = getTargetData();
        if (targetData.controlPoints() != null && !targetData.controlPoints().isEmpty()) {
            if (this.path == null) {
                setupPath();
            } else {
                setPosition(level);
                setFacingDirection();
                distanceNearestBlockTarget = 7;
            }
        } else this.discard();
    }

    private void setupPath() {
        NewMLBTargetData targetData = this.getTargetData();
        path = new MineraculousMathUtils.CatmullRom(targetData.controlPoints());
        setSplinePosition((float) path.getFirstParameter());
    }

    private void setPosition(Level level) {
        if (level instanceof ServerLevel) {
            double speed = MineraculousServerConfig.get().miraculousLadybugSpeed.get() / 100f;
            double splinePositionParameter = getSplinePosition();
            splinePositionParameter = path.advanceParameter(splinePositionParameter, speed);
            this.moveTo(path.getPoint(splinePositionParameter));
            setSplinePosition((float) splinePositionParameter);
        }
    }

    private void setFacingDirection() {
        NewMLBTargetData targetData = getTargetData();
        double splinePositionParameter = getSplinePosition();
        Vec3 tangent = path.getDerivative(splinePositionParameter).normalize();
        double yaw = Math.toDegrees(Math.atan2(tangent.z, tangent.x)) - 90.0;
        double pitch = Math.toDegrees(-Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
    }
}
