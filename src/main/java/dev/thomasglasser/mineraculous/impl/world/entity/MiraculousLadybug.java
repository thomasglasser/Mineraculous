package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybug extends Entity {
    public MineraculousMathUtils.CatmullRom path = null;
    public double oldSplinePosition = 0;
    private double distanceNearestTarget = 0;

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
                setDistanceNearestBlockTarget(level, targetData);
            }
            if (!level.isClientSide()) {
                targetData.withSplinePosition(splinePositionParameter).save(this, true);
            } else {
                renderParticles();
            }
        } else this.discard();
    }

    private void setDistanceNearestBlockTarget(Level level, MiraculousLadybugTargetData targetData) {
        if (level.isClientSide()) {
            double distance = Double.MAX_VALUE;
            for (MiraculousLadybugTargetData.BlockTarget target : targetData.blockTargets()) {
                distance = Math.min(distance, target.position().distanceTo(this.position()));
            }
            this.distanceNearestTarget = distance;
        }
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
        splinePositionParameter = path.advanceParameter(splinePositionParameter, 0.8); //0.8
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

    // TODO: Call when reverting at pos
    private void revert() {
//        related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotization -> {
//            Kamikotization value = kamikotization.value();
//            AbilityData abilityData = new AbilityData(0, false);
//            value.powerSource().ifLeft(tool -> {
//                if (tool.getItem() instanceof EffectRevertingItem item) {
//                    item.revert(related);
//                }
//            }).ifRight(ability -> ability.value().revert(abilityData, level, related, ));
//            value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//        });
//        MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
//        for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
//            Miraculous value = miraculous.value();
//            AbilityData abilityData = new AbilityData(miraculousesData.get(miraculous).powerLevel(), false);
//            value.activeAbility().value().revert(abilityData, level, related, );
//            value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, ));
//        }
    }

    public double getDistanceToNearestBlockTarget() {
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
