package dev.thomasglasser.mineraculous.world.entity.projectile;

import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    private static final EntityDataAccessor<Float> DATA_SERVER_MAX_ROPE_LENGTH = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RENDER_MAX_ROPE_LENGTH = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Direction> DATA_INITIAL_DIRECTION = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> DATA_RECALLING_TICKS = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INITIAL_HAND = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean dealtDamage;

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
        builder.define(DATA_SERVER_MAX_ROPE_LENGTH, 0f);
        builder.define(DATA_RENDER_MAX_ROPE_LENGTH, 0f);
        builder.define(DATA_INITIAL_DIRECTION, Direction.UP);
        builder.define(DATA_INITIAL_HAND, 0);
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

    public float getServerMaxRopeLength() {
        return this.entityData.get(DATA_SERVER_MAX_ROPE_LENGTH);
    }

    public void setServerMaxRopeLength(float f) {
        this.entityData.set(DATA_SERVER_MAX_ROPE_LENGTH, Math.max(f, 1.5f));
    }

    public float getRenderMaxRopeLength() {
        return this.entityData.get(DATA_RENDER_MAX_ROPE_LENGTH);
    }

    public void setRenderMaxRopeLength(float f) {
        this.entityData.set(DATA_RENDER_MAX_ROPE_LENGTH, Math.max(f, 1.5f));
    }

    public Direction getInitialDirection() {
        return this.entityData.get(DATA_INITIAL_DIRECTION);
    }

    public void setInitialDirection(Direction dir) {
        this.entityData.set(DATA_INITIAL_DIRECTION, dir);
    }

    public InteractionHand getInitialHand() {
        return InteractionHand.values()[this.entityData.get(DATA_INITIAL_HAND)];
    }

    public void setInitialHand(InteractionHand hand) {
        this.entityData.set(DATA_INITIAL_HAND, hand.ordinal());
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

            if (this.isRecalling()) {
                this.setNoPhysics(true);
                Vec3 vec3 = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                double distance = vec3.length();
                vec3.normalize();
                this.setDeltaMovement(vec3.scale(Math.min(Math.max(distance * 0.01 * 2.5, 0.3), 0.5)));
                if (distance <= 2 || distance > this.getServerMaxRopeLength() + 1 || this.getRecallingTicks() >= 15) {
                    this.discard();
                }
                this.updateRecallingTicks();
            } else if (this.inGround()) {
                Vec3 fromProjectileToPlayer = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                double distance = fromProjectileToPlayer.length();

                if (distance > this.getServerMaxRopeLength() && this.getServerMaxRopeLength() > 0 && distance <= 99) {
                    owner.resetFallDistance();
                    Vec3 constrainedPosition = owner.position()
                            .add(fromProjectileToPlayer.normalize().scale(this.getServerMaxRopeLength() - distance));
                    normalCollisions(false, owner);
                    if (!owner.level().getBlockState(new BlockPos((int) constrainedPosition.x, (int) (constrainedPosition.y + 0.5), (int) constrainedPosition.z)).isSolid()) {
                        owner.setPos(constrainedPosition.x, constrainedPosition.y, constrainedPosition.z);
                    }

                    Vec3 radialForce = fromProjectileToPlayer.normalize();
                    Vec3 tangentialVelocity = owner.getDeltaMovement().subtract(
                            radialForce.scale(owner.getDeltaMovement().dot(radialForce)));
                    double dampingFactor = Math.max(1.06, 1 - Math.abs(distance - this.getServerMaxRopeLength()) * 0.02); // Less damping near center
                    Vec3 dampedVelocity = tangentialVelocity.scale(dampingFactor);

                    Vec3 correctiveForce = radialForce.scale((distance - this.getServerMaxRopeLength()) * 0.005);
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

    public void normalCollisions(boolean sliding, LivingEntity owner) {
        // stop if collided with object
        if (owner.horizontalCollision) {
            if (owner.getDeltaMovement().x == 0) {
                if (!sliding || tryStepUp(new Vec3(owner.getDeltaMovement().x, 0, 0), owner)) {
                    owner.setDeltaMovement(0, owner.getDeltaMovement().y, owner.getDeltaMovement().z);
                }
            }
            if (owner.getDeltaMovement().z == 0) {
                if (!sliding || tryStepUp(new Vec3(0, 0, owner.getDeltaMovement().z), owner)) {
                    owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, 0);
                }
            }
        }

        if (sliding && !owner.horizontalCollision) {
            if (owner.position().x - owner.xOld == 0) {
                owner.setDeltaMovement(0, owner.getDeltaMovement().y, owner.getDeltaMovement().z);
            }
            if (owner.position().z - owner.zOld == 0) {
                owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, 0);
            }
        }

        if (owner.verticalCollision) {
            if (owner.onGround()) {
                if (!sliding && owner.getDeltaMovement().y > 0) {
                    owner.setDeltaMovement(owner.getDeltaMovement().x, owner.getDeltaMovement().y, owner.getDeltaMovement().z);
                } else {
                    if (owner.getDeltaMovement().y < 0) {
                        owner.setDeltaMovement(owner.getDeltaMovement().x, 0, owner.getDeltaMovement().z);
                    }
                }
            } else {
                if (owner.getDeltaMovement().y > 0) {
                    if (owner.yOld == owner.position().y) {
                        owner.setDeltaMovement(owner.getDeltaMovement().x, 0, owner.getDeltaMovement().z);
                    }
                }
            }
        }
    }

    public boolean tryStepUp(Vec3 collisionMotion, LivingEntity owner) {
        if (collisionMotion.length() == 0) {
            return false;
        }
        Vec3 moveOffset = collisionMotion.normalize().scale(0.05).add(0, 0.5 + 0.01, 0);
        Iterable<VoxelShape> collisions = owner.level().getCollisions(owner, owner.getBoundingBox().move(moveOffset.x, moveOffset.y, moveOffset.z));
        if (!collisions.iterator().hasNext()) {
            if (!owner.onGround()) {
                Vec3 pos = new Vec3(owner.getX(), owner.getY(), owner.getZ());
                pos.add(moveOffset);
                owner.setPos(pos);
                owner.xOld = pos.x;
                owner.yOld = pos.y;
                owner.zOld = pos.z;
            }
            owner.horizontalCollision = false;
            return false;
        }
        return true;
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
        ItemStack stack = owner.getItemInHand(getInitialHand());
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
                MineraculousEntityEvents.tryBreakItemEntity(result, itemEntity, serverLevel, position());
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
        } else if (ability == LadybugYoyoItem.Ability.LASSO) {
            // TODO: Lasso mode more like lead
        } else if (ability == LadybugYoyoItem.Ability.PURIFY && entity instanceof Kamiko kamiko && level() instanceof ServerLevel serverLevel) {
            ResolvableProfile profile = getPickupItemStackOrigin().get(DataComponents.PROFILE);
            Player yoyoOwner = profile != null ? serverLevel.getPlayerByUUID(profile.id().orElse(profile.gameProfile().getId())) : null;
            if (yoyoOwner != null) {
                MiraculousesData miraculousesData = yoyoOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                ResourceKey<Miraculous> storingKey = miraculousesData.getFirstTransformedKeyIn(MiraculousTags.CAN_USE_LADYBUG_YOYO, serverLevel.registryAccess());
                MiraculousData storingData = miraculousesData.get(storingKey);
                if (storingData != null) {
                    CompoundTag kamikoData = kamiko.saveWithoutId(new CompoundTag());
                    ListTag list = storingData.extraData().getList(LadybugYoyoItem.TAG_STORED_KAMIKOS, 10);
                    list.add(kamikoData);
                    kamiko.discard();
                    storingData.extraData().put(LadybugYoyoItem.TAG_STORED_KAMIKOS, list);
                    miraculousesData.put(yoyoOwner, storingKey, storingData, true);
                    discard();
                }
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
                        updateRenderMaxRopeLength(player);
                    }

                    Vec3 fromProjectileToPlayer = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ());
                    this.setServerMaxRopeLength((float) fromProjectileToPlayer.length() + 1.5f);
                }
            } else {
                recall();
            }
        }
    }

    public void updateRenderMaxRopeLength(Player p) {
        float f = p.getAttackAnim(0);
        float f1 = Mth.sin(Mth.sqrt(f) * 3.1415927F);

        // TODO: Fix dedicated server crash
        Vec3 vec3 = ThrownLadybugYoyoRenderer.getPlayerHandPos(p, f1, 0, !p.getMainHandItem().is(MineraculousItems.LADYBUG_YOYO));
        Vec3 fromProjectileToHand = new Vec3(vec3.x - this.getX(), vec3.y - this.getY(), vec3.z - this.getZ());
        setRenderMaxRopeLength((float) fromProjectileToHand.length());
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
