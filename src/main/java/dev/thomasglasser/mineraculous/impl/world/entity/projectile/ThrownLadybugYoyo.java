package dev.thomasglasser.mineraculous.impl.world.entity.projectile;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundCalculateYoyoRenderLengthPayload;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownLadybugYoyo extends AbstractArrow implements GeoEntity {
    private static final EntityDataAccessor<Optional<LadybugYoyoItem.Ability>> DATA_ABILITY = SynchedEntityData.defineId(ThrownLadybugYoyo.class, MineraculousEntityDataSerializers.OPTIONAL_LADYBUG_YOYO_ABILITY.get());
    private static final EntityDataAccessor<Boolean> DATA_IS_RECALLING = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_MAX_ROPE_LENGTH = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Direction> DATA_INITIAL_DIRECTION = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> DATA_RECALLING_TICKS = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HAND = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean dealtDamage;
    private boolean freshlyHitGround = false;
    public float renderMaxRopeLength = 0;
    public float firstPovRenderMaxRopeLength = 0;

    public ThrownLadybugYoyo(EntityType<? extends ThrownLadybugYoyo> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        setBaseDamage(8);
    }

    public ThrownLadybugYoyo(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable LadybugYoyoItem.Ability ability) {
        super(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), owner, level, pickupItemStack, pickupItemStack);
        this.noCulling = true;
        setBaseDamage(8);
        setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        setAbility(ability);
        new ThrownLadybugYoyoData(this.getId()).save(owner, !level.isClientSide);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ABILITY, Optional.empty());
        builder.define(DATA_IS_RECALLING, false);
        builder.define(DATA_RECALLING_TICKS, 0);
        builder.define(DATA_MAX_ROPE_LENGTH, 0f);
        builder.define(DATA_INITIAL_DIRECTION, Direction.UP);
        builder.define(DATA_HAND, 0);
    }

    public @Nullable LadybugYoyoItem.Ability getAbility() {
        return this.entityData.get(DATA_ABILITY).orElse(null);
    }

    public void setAbility(@Nullable LadybugYoyoItem.Ability ability) {
        this.entityData.set(DATA_ABILITY, Optional.ofNullable(ability));
    }

    public boolean isRecalling() {
        return this.entityData.get(DATA_IS_RECALLING);
    }

    private int getRecallingTicks() {
        return this.entityData.get(DATA_RECALLING_TICKS);
    }

    private void updateRecallingTicks() {
        this.entityData.set(DATA_RECALLING_TICKS, this.getRecallingTicks() + 1);
    }

    public float getMaxRopeLength() {
        return this.entityData.get(DATA_MAX_ROPE_LENGTH);
    }

    public void setMaxRopeLength(float f) {
        this.entityData.set(DATA_MAX_ROPE_LENGTH, Math.max(f, 1.5f));
    }

    public float getRenderMaxRopeLength(boolean isFirstPov) {
        return isFirstPov ? this.firstPovRenderMaxRopeLength : this.renderMaxRopeLength;
    }

    public void setRenderMaxRopeLength(float f) {
        this.renderMaxRopeLength = f;
    }

    public void setFirstPovRenderMaxRopeLength(float f) {
        this.firstPovRenderMaxRopeLength = f;
    }

    public Direction getInitialDirection() {
        return this.entityData.get(DATA_INITIAL_DIRECTION);
    }

    public void setInitialDirection(Direction dir) {
        this.entityData.set(DATA_INITIAL_DIRECTION, dir);
    }

    public InteractionHand getHand() {
        return InteractionHand.values()[this.entityData.get(DATA_HAND)];
    }

    public void setHand(InteractionHand hand) {
        this.entityData.set(DATA_HAND, hand.ordinal());
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof Player player ? player : null;
    }

    public boolean inGround() {
        if (!isRecalling())
            return this.inGround;
        else return false;
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = getOwner();
        if (entity == null) {
            discard();
            return;
        }
        if (entity instanceof LivingEntity owner) {
            checkInstantRecall(owner);
            if (this.freshlyHitGround && !this.isRecalling()) {
                Vec3 fromProjectileToPlayer = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                this.setMaxRopeLength((float) fromProjectileToPlayer.length());
                this.freshlyHitGround = false;
            }
            if (this.isRecalling()) {
                this.setNoPhysics(true);
                Vec3 vec3 = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                double distance = vec3.length();
                vec3.normalize();
                this.setDeltaMovement(vec3.scale(Math.min(Math.max(distance * 0.01 * 2.5, 0.3), 0.5)));
                if (distance <= 2 || distance > this.getMaxRopeLength() + 1 || this.getRecallingTicks() >= 15) {
                    this.discard();
                }
                this.updateRecallingTicks();
            } else if (this.inGround()) {
                Vec3 fromProjectileToPlayer = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                float distance = (float) fromProjectileToPlayer.length();
                if (distance > this.getMaxRopeLength() && this.getMaxRopeLength() > 0 && distance <= 99) {
                    owner.resetFallDistance();

                    Vec3 constrainedMovement = fromProjectileToPlayer.normalize().scale(this.getMaxRopeLength() - distance);
                    owner.move(MoverType.SELF, constrainedMovement);

                    Vec3 radialForce = fromProjectileToPlayer.normalize();

                    Vec3 tangentialVelocity = owner.getDeltaMovement().subtract(
                            radialForce.scale(owner.getDeltaMovement().dot(radialForce)));

                    /* if (!ownerInput.equals(Vec3.ZERO) && tangentialVelocity.lengthSqr() > 1e-6) {
                        // Rope direction (to detect bottom vs top)
                        Vec3 ropeDir = fromProjectileToPlayer.normalize();
                        double cosAngle = ropeDir.dot(new Vec3(0, -1, 0));
                    
                        // Pump factor: max at bottom, fades near top
                        double pumpFactor = Math.max(0, Math.pow(1 - cosAngle * cosAngle, 2.0));
                    
                        double inertiaBoost = 1.0 + (10.0 * pumpFactor);
                        tangentialVelocity = tangentialVelocity.scale(inertiaBoost);
                    
                        if (pumpFactor > 0.8) {
                            tangentialVelocity = tangentialVelocity.scale(10);
                        }
                    
                        owner.addDeltaMovement(ownerInput.scale(1));
                    }*/

                    double dampingFactor = Math.max(1.06, 1 - Math.abs(distance - this.getMaxRopeLength()) * 0.02); // Less damping near center
                    Vec3 dampedVelocity = tangentialVelocity.scale(dampingFactor);

                    Vec3 correctiveForce = radialForce.scale((distance - this.getMaxRopeLength()) * 0.005);
                    Vec3 newVelocity = dampedVelocity.add(correctiveForce);

                    if (this.getY() > owner.getY()) {
                        owner.setDeltaMovement(newVelocity);
                    }
                }
            } else {
                if (this.tickCount < 50) {
                    if (owner.onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(3));
                    } else {
                        Vec3 motion = owner.getLookAngle().scale(4); //this makes it follow the cursor
                        this.setDeltaMovement(motion);
                    }
                } else {
                    this.setNoGravity(false);
                }
            }
        }

        super.tick();
    }

    public void recall() {
        this.entityData.set(DATA_IS_RECALLING, true);
        this.entityData.set(DATA_RECALLING_TICKS, 0);
    }

    @Override
    protected void tickDespawn() {}

    private void checkInstantRecall(LivingEntity owner) {
        Vec3 fromProjectileToPlayer = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
        double distance = fromProjectileToPlayer.length();
        ItemStack stack = owner.getItemInHand(getHand());
        boolean flag = distance <= 100
                && this.level().dimension() == owner.level().dimension()
                && stack.is(MineraculousItems.LADYBUG_YOYO)
                && stack.getOrDefault(MineraculousDataComponents.ACTIVE, false)
                && (getAbility() == null || getAbility() == stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY));
        if (owner.isRemoved() || !flag) {
            this.discard();
        }
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.getAbility() == LadybugYoyoItem.Ability.TRAVEL || this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();
        LadybugYoyoItem.Ability ability = this.getAbility();
        if (ability == null) {
            float damage = (float) getBaseDamage();
            DamageSource damagesource = this.damageSources().arrow(this, owner == null ? this : owner);
            if (this.level() instanceof ServerLevel serverLevel) {
                damage = EnchantmentHelper.modifyDamage(serverLevel, getPickupItem(), entity, damagesource, damage);
            }

            this.dealtDamage = true;
            if (entity instanceof ItemEntity itemEntity && level() instanceof ServerLevel serverLevel) {
                MineraculousEntityUtils.tryBreakItemEntity(itemEntity, serverLevel);
            } else if (entity.hurt(damagesource, damage)) {
                if (entity.getType() == EntityType.ENDERMAN) {
                    return;
                }

                if (this.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damagesource, this.getWeaponItem());
                }

                if (entity instanceof LivingEntity livingentity) {
                    this.doKnockback(livingentity, damagesource);
                    this.doPostHurtEffects(livingentity);
                }
            }
            recall();
        } else if (level() instanceof ServerLevel level) {
            if (ability == LadybugYoyoItem.Ability.PURIFY) {
                AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
                if (entityData.isConverted(entity.getUUID())) {
                    UUID ownerId = getPickupItemStackOrigin().get(MineraculousDataComponents.OWNER);
                    Entity yoyoOwner = ownerId != null ? level.getEntities().get(ownerId) : null;
                    if (yoyoOwner != null) {
                        MiraculousesData miraculousesData = yoyoOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                        Holder<Miraculous> storingKey = miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_LADYBUG_YOYO);
                        MiraculousData storingData = miraculousesData.get(storingKey);
                        if (storingData != null) {
                            CompoundTag tag = new CompoundTag();
                            entity.save(tag);
                            List<CompoundTag> stored = storingData.storedEntities();
                            stored.add(tag);
                            entity.discard();
                            storingData.save(storingKey, yoyoOwner, true);
                            discard();
                        }
                    }
                }
            } else if (ability == LadybugYoyoItem.Ability.LASSO && owner != null && entity instanceof Leashable leashable) {
                if (leashable.getLeashHolder() != owner && entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_HOLDER).isEmpty()) {
                    if (leashable.isLeashed()) {
                        leashable.dropLeash(true, true);
                    }
                    leashable.setLeashedTo(owner, true);
                    entity.setData(MineraculousAttachmentTypes.YOYO_LEASH_HOLDER, Optional.of(owner.getUUID()));
                    TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.YOYO_LEASH_HOLDER, Optional.of(owner.getUUID())), owner.getServer());
                    new LeashingLadybugYoyoData(entity.getId()).save(owner, true);
                }
                recall();
            }
        }
        this.playSound(getHitGroundSoundEvent());
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (getAbility() == null && !target.canBeHitByProjectile() && !(target instanceof ItemEntity)) {
            return false;
        } else {
            Entity entity = this.getOwner();
            return entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(target);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            if (getAbility() == LadybugYoyoItem.Ability.TRAVEL) {
                Entity owner = this.getOwner();
                if (owner != null && this.inGround() && !this.isRecalling()) {
                    if (owner instanceof Player player) {
                        TommyLibServices.NETWORK.sendToAllClients(new ClientboundCalculateYoyoRenderLengthPayload(this.getId(), player.getId()), player.getServer());
                    }
                    this.freshlyHitGround = true;
                }
            } else {
                recall();
            }
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return false;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return MineraculousItems.LADYBUG_YOYO.toStack();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player entity) {
        if (this.ownedBy(entity) || this.getOwner() == null) {
            super.playerTouch(entity);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Ability"))
            setAbility(LadybugYoyoItem.Ability.valueOf(compound.getString("Ability")));
        this.dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getAbility() != null)
            compound.putString("Ability", this.getAbility().name());
        compound.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (getAbility() == LadybugYoyoItem.Ability.PURIFY)
                return state.setAndContinue(DefaultAnimations.IDLE);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (getOwner() != null) {
            if (!level().isClientSide)
                getOwner().getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).clearId().save(getOwner(), true);
        }
        super.remove(reason);
    }
}
