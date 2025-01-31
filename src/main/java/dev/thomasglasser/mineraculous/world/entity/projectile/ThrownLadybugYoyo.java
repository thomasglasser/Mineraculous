package dev.thomasglasser.mineraculous.world.entity.projectile;

import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.network.ClientboundSyncLadybugYoyoPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownLadybugYoyo extends AbstractArrow implements GeoEntity {
    private static final EntityDataAccessor<Optional<LadybugYoyoItem.Ability>> ABILITY = SynchedEntityData.defineId(ThrownLadybugYoyo.class, MineraculousEntityDataSerializers.OPTIONAL_LADYBUG_YOYO_ABILITY.get());
    private static final EntityDataAccessor<Vector3f> BOUND_POS = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> IS_RECALLING = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SERVER_MAX_ROPE_LENGTH = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RECALLING_TICKS = SynchedEntityData.defineId(ThrownLadybugYoyo.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public double maxRopeLength = 0; // used for rendering the rope

    private boolean dealtDamage;

    public ThrownLadybugYoyo(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable LadybugYoyoItem.Ability ability) {
        super(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), owner, level, pickupItemStack, null);
        setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        setAbility(ability);
        owner.setData(MineraculousAttachmentTypes.LADYBUG_YOYO, Optional.of(this.getId()));
        if (level instanceof ServerLevel serverLevel) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncLadybugYoyoPayload(Optional.of(this.getId())), serverLevel.getServer());
        }
    }

    public ThrownLadybugYoyo(double x, double y, double z, Level level, ItemStack pickupItemStack, @Nullable LadybugYoyoItem.Ability ability) {
        super(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), x, y, z, level, pickupItemStack, null);
        setAbility(ability);
    }

    public ThrownLadybugYoyo(EntityType<? extends ThrownLadybugYoyo> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ABILITY, Optional.empty());
        builder.define(BOUND_POS, Vec3.ZERO.toVector3f());
        builder.define(IS_RECALLING, false);
        builder.define(RECALLING_TICKS, 0);
        builder.define(SERVER_MAX_ROPE_LENGTH, 0f);
    }

    public @Nullable LadybugYoyoItem.Ability getAbility() {
        return this.entityData.get(ABILITY).orElse(null);
    }

    public void setAbility(@Nullable LadybugYoyoItem.Ability ability) {
        this.entityData.set(ABILITY, Optional.ofNullable(ability));
    }

    public @Nullable Vec3 getBoundPos() {
        return this.entityData.get(BOUND_POS).equals(Vec3.ZERO.toVector3f()) ? null : new Vec3(this.entityData.get(BOUND_POS));
    }

    public void setBoundPos(@Nullable Vec3 boundPos) {
        this.entityData.set(BOUND_POS, boundPos == null ? Vec3.ZERO.toVector3f() : boundPos.toVector3f());
    }

    public boolean isBound() {
        return getBoundPos() != null;
    }

    public void clearBoundPos() {
        setBoundPos(null);
    }

    public boolean isRecalling() {
        return this.entityData.get(IS_RECALLING);
    }

    private int getRecallingTicks() {
        return this.entityData.get(RECALLING_TICKS);
    }

    private void updateRecallingTicks() {
        this.entityData.set(RECALLING_TICKS, this.getRecallingTicks() + 1);
    }

    public float getServerMaxRopeLength() {
        return this.entityData.get(SERVER_MAX_ROPE_LENGTH);
    }

    public void setServerMaxRopeLength(float f) {
        this.entityData.set(SERVER_MAX_ROPE_LENGTH, f);
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
        super.tick();
        if (getBoundPos() != null) {
            this.teleportTo(getBoundPos().x(), getBoundPos().y(), getBoundPos().z());
            return;
        }
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity entity = getOwner();
        if (entity == null)
            return;
        if (entity instanceof ServerPlayer player) {
            checkRecall(player);

            if (this.isRecalling()) {
                this.setNoPhysics(true);
                Vec3 vec3 = new Vec3(player.getX() - this.getX(), player.getY() - this.getY(), player.getZ() - this.getZ());
                double distance = vec3.length();
                vec3.normalize();
                this.setDeltaMovement(vec3.scale(Math.min(Math.max(distance * 0.01 * 2.5, 0.3), 0.5)));
                if (distance <= 2 || distance > this.getServerMaxRopeLength() + 1 || this.getRecallingTicks() >= 15) {
                    this.discard();
                }
                this.updateRecallingTicks();
            } else if (this.inGround()) {
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - this.getX(), player.getY() - this.getY(), player.getZ() - this.getZ());
                double distance = fromProjectileToPlayer.length();

                if (distance > this.getServerMaxRopeLength()) {
                    player.resetFallDistance();
                    Vec3 constrainedPosition = player.position()
                            .add(fromProjectileToPlayer.normalize().scale(this.getServerMaxRopeLength() - distance));
                    normalCollisions(false, player);
                    if (player.level().isEmptyBlock(new BlockPos((int) constrainedPosition.x, (int) (constrainedPosition.y + 0.5), (int) constrainedPosition.z))) {
                        player.setPos(constrainedPosition.x, constrainedPosition.y, constrainedPosition.z);
                    }

                    Vec3 radialForce = fromProjectileToPlayer.normalize();
                    Vec3 tangentialVelocity = player.getDeltaMovement().subtract(
                            radialForce.scale(player.getDeltaMovement().dot(radialForce)));
                    double dampingFactor = Math.max(1.06, 1 - Math.abs(distance - this.getServerMaxRopeLength()) * 0.02); // Less damping near center
                    Vec3 dampedVelocity = tangentialVelocity.scale(dampingFactor);

                    Vec3 correctiveForce = radialForce.scale((distance - this.getServerMaxRopeLength()) * 0.005);
                    Vec3 newVelocity = dampedVelocity.add(correctiveForce);

                    if (this.getY() > player.getY()) {
                        player.setDeltaMovement(newVelocity);
                    }

                }
            } else {
                if (this.tickCount < 50) {
                    if (player.onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(3));
                    } else {
                        Vec3 motion = player.getLookAngle().scale(4); //this makes it follow the cursor
                        this.setDeltaMovement(motion);
                    }
                } else {
                    this.setNoGravity(false);
                }
            }
        }
    }

    public void normalCollisions(boolean sliding, Player entity) {
        // stop if collided with object
        if (entity.horizontalCollision) {
            if (entity.getDeltaMovement().x == 0) {
                if (!sliding || tryStepUp(new Vec3(entity.getDeltaMovement().x, 0, 0), entity)) {
                    entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                }
            }
            if (entity.getDeltaMovement().z == 0) {
                if (!sliding || tryStepUp(new Vec3(0, 0, entity.getDeltaMovement().z), entity)) {
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
                }
            }
        }

        if (sliding && !entity.horizontalCollision) {
            if (entity.position().x - entity.xOld == 0) {
                entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
            }
            if (entity.position().z - entity.zOld == 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
            }
        }

        if (entity.verticalCollision) {
            if (entity.onGround()) {
                if (!sliding && Minecraft.getInstance().options.keyJump.isDown()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                } else {
                    if (entity.getDeltaMovement().y < 0) {
                        entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
                    }
                }
            } else {
                if (entity.getDeltaMovement().y > 0) {
                    if (entity.yOld == entity.position().y) {
                        entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
                    }
                }
            }
        }
    }

    public boolean tryStepUp(Vec3 collisionmotion, Player entity) {
        if (collisionmotion.length() == 0) {
            return false;
        }
        Vec3 moveoffset = collisionmotion.normalize().scale(0.05).add(0, 0.5 + 0.01, 0);
        Iterable<VoxelShape> collisions = entity.level().getCollisions(entity, entity.getBoundingBox().move(moveoffset.x, moveoffset.y, moveoffset.z));
        if (!collisions.iterator().hasNext()) {
            if (!entity.onGround()) {
                Vec3 pos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
                pos.add(moveoffset);
                entity.setPos(pos);
                entity.xOld = pos.x;
                entity.yOld = pos.y;
                entity.zOld = pos.z;
            }
            entity.horizontalCollision = false;
            return false;
        }
        return true;
    }

    public void recall() {
        this.entityData.set(IS_RECALLING, true);
        this.entityData.set(RECALLING_TICKS, 0);
    }

    @Override
    protected void tickDespawn() {}

    private void checkRecall(Player player) {
        ItemStack itemstack = player.getMainHandItem();
        boolean flag = itemstack.is(MineraculousItems.LADYBUG_YOYO);
        if (!(!player.isRemoved() && player.isAlive() && flag)) {
            this.discard();
        }
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level() instanceof ServerLevel serverlevel) {
            if (getAbility() == null) {
                Entity entity = result.getEntity();
                float f = 8.0F;
                Entity entity1 = this.getOwner();
                DamageSource damagesource = this.damageSources().trident(this, entity1 == null ? this : entity1);
                f = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, f);

                this.dealtDamage = true;
                if (entity.hurt(damagesource, f)) {
                    if (entity.getType() == EntityType.ENDERMAN) {
                        return;
                    }

                    if (this.level() instanceof ServerLevel serverlevel1) {
                        EnchantmentHelper.doPostAttackEffectsWithItemSource(
                                serverlevel1, entity, damagesource, this.getWeaponItem());
                    }

                    if (entity instanceof LivingEntity livingentity) {
                        this.doKnockback(livingentity, damagesource);
                        this.doPostHurtEffects(livingentity);
                    }
                }

                this.discard();
                this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.0F);
            } else if (getAbility() == LadybugYoyoItem.Ability.LASSO) {
                Entity main = result.getEntity();
                Vec3 boundPos = main.position().add(0, 0.5, 0);
                setBoundPos(boundPos);
                setDeltaMovement(Vec3.ZERO);
                teleportTo(boundPos.x(), boundPos.y(), boundPos.z());
                List<Entity> list = this.level().getEntities(getOwner(), main.getBoundingBox().inflate(2, 1, 2), entity -> entity != this);
                for (Entity entity : list) {
                    entity.teleportTo(main.getX(), main.getY(), main.getZ());
                    CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(entity);
                    CompoundTag pos = new CompoundTag();
                    pos.putDouble("X", main.getX());
                    pos.putDouble("Y", main.getY());
                    pos.putDouble("Z", main.getZ());
                    entityData.put(MineraculousEntityEvents.TAG_YOYO_BOUND_POS, pos);
                    TommyLibServices.ENTITY.setPersistentData(entity, entityData, true);
                }
            } else if (getAbility() == LadybugYoyoItem.Ability.PURIFY && getOwner() != null && result.getEntity() instanceof Kamiko kamiko && kamiko.isPowered()) {
                MiraculousDataSet miraculousDataSet = getOwner().getData(MineraculousAttachmentTypes.MIRACULOUS);
                MiraculousData data = miraculousDataSet.get(MineraculousMiraculous.LADYBUG);
                CompoundTag kamikoData = kamiko.saveWithoutId(new CompoundTag());
                ListTag list = data.extraData().getList(LadybugYoyoItem.TAG_STORED_KAMIKOS, 10);
                list.add(kamikoData);
                kamiko.discard();
                data.extraData().put(LadybugYoyoItem.TAG_STORED_KAMIKOS, list);
                miraculousDataSet.put(getOwner(), MineraculousMiraculous.LADYBUG, data, true);
                discard();
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity entity = this.getOwner();
        return entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(target);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult hitResult, ItemStack stack) {
        Vec3 vec3 = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        EnchantmentHelper.onHitBlock(
                level,
                stack,
                this.getOwner() instanceof LivingEntity livingentity ? livingentity : null,
                this,
                null,
                vec3,
                level.getBlockState(hitResult.getBlockPos()),
                p_375966_ -> this.kill());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (getAbility() != LadybugYoyoItem.Ability.LASSO || getBoundPos() == null) {
            super.onHitBlock(result);
            if (this.level() instanceof ServerLevel) {
                if (getAbility() == LadybugYoyoItem.Ability.TRAVEL) {
                    Player p = this.getPlayerOwner();
                    if (p != null && this.inGround() && !this.isRecalling()) {

                        updateRenderMaxRopeLength(p);

                        Vec3 fromProjectileToPlayer = new Vec3(p.getX() - this.getX(), p.getY() - this.getY(), p.getZ() - this.getZ());
                        this.setServerMaxRopeLength((float) fromProjectileToPlayer.length() + 1.5f);
                    }
                } else {
                    recall();
                }
            }
        }
    }

    public void updateRenderMaxRopeLength(Player p) {
        float f = p.getAttackAnim(0);
        float f1 = Mth.sin(Mth.sqrt(f) * 3.1415927F);

        Vec3 vec3 = ThrownLadybugYoyoRenderer.getPlayerHandPos(p, f1, 0, MineraculousItems.LADYBUG_YOYO.get(), Minecraft.getInstance().getEntityRenderDispatcher());
        Vec3 fromProjectileToHand = new Vec3(vec3.x - this.getX(), vec3.y - this.getY(), vec3.z - this.getZ());
        this.maxRopeLength = fromProjectileToHand.length();
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
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
                return state.setAndContinue(LadybugYoyoItem.OPEN_IDLE);
            return state.setAndContinue(DefaultAnimations.IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (getOwner() != null) {
            getOwner().setData(MineraculousAttachmentTypes.LADYBUG_YOYO, Optional.empty());
            if (level() instanceof ServerLevel serverLevel) {
                TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncLadybugYoyoPayload(Optional.empty()), serverLevel.getServer());
            }
        }
        super.remove(reason);
    }
}
