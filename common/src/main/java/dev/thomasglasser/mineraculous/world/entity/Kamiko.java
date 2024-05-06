package dev.thomasglasser.mineraculous.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Evil form is known as an Akuma.
 */
public class Kamiko extends AmbientCreature {
    private static final EntityDataAccessor<Boolean> RESTING = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);

    public Kamiko(EntityType<? extends Kamiko> type, Level level) {
        super(type, level);
        if (!level.isClientSide) this.setResting(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RESTING, false);
        builder.define(POWERED, false);
    }

    public boolean isPowered() {
        return entityData.get(POWERED);
    }

    public void setPowered(boolean powered) {
        entityData.set(POWERED, powered);
    }

    public boolean isResting() {
        return entityData.get(RESTING);
    }

    public void setResting(boolean resting) {
        entityData.set(RESTING, resting);
    }

    @Override // Silent
    protected float getSoundVolume() {
        return 0;
    }

    // No Push
    public boolean isPushable() {
        return false;
    }
    protected void doPush(Entity entity) {}
    protected void pushEntities() {}

    @Override
    public void tick() {
        super.tick();
        if (this.isResting()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), Mth.floor(this.getY()), this.getZ());
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    // Flying mobs don't take fall damage.
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Kamikos can squeeze through impossibly tight spaces. Including between atomic particles.
        return level().damageSources().source(DamageTypes.IN_WALL).equals(source) || super.isInvulnerableTo(source);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.level().isClientSide && this.isResting()) {
                this.setResting(false);
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(RESTING, tag.getBoolean("Resting"));
        this.entityData.set(POWERED, tag.getBoolean("Powered"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Resting", this.entityData.get(RESTING));
        tag.putBoolean("Powered", this.entityData.get(POWERED));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1) // Butterflies are weak, okay.
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.3);
    }
}
