package dev.thomasglasser.mineraculous.world.entity.projectile;

import static dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload.ITEM_UNBREAKABLE_KEY;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownButterflyCane extends AbstractArrow implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> DATA_SHOW_BLADE = SynchedEntityData.defineId(ThrownButterflyCane.class, EntityDataSerializers.BOOLEAN);

    private boolean dealtDamage;

    public ThrownButterflyCane(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownButterflyCane(LivingEntity owner, Level level, ItemStack pickupItemStack) {
        super(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), owner, level, pickupItemStack, null);
        setShowBlade(pickupItemStack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.BLADE);
    }

    public ThrownButterflyCane(double x, double y, double z, Level level, ItemStack pickupItemStack) {
        super(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), x, y, z, level, pickupItemStack, null);
        setShowBlade(pickupItemStack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.BLADE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHOW_BLADE, false);
    }

    public void setShowBlade(boolean showBlade) {
        this.entityData.set(DATA_SHOW_BLADE, showBlade);
    }

    public boolean showBlade() {
        return this.entityData.get(DATA_SHOW_BLADE);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return MineraculousItems.BUTTERFLY_CANE.toStack();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level() instanceof ServerLevel serverlevel) {
            Entity entity = result.getEntity();
            float f = 8.0F;
            Entity owner = this.getOwner();
            DamageSource damagesource = this.damageSources().trident(this, owner == null ? this : owner);
            f = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, f);

            this.dealtDamage = true;
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem().copy();
                ItemStack rest = stack.copyWithCount(stack.getCount() - 1);
                stack.setCount(1);
                if (stack.isDamageableItem()) {
                    int i = 100;
                    MiraculousesData data = owner != null ? owner.getData(MineraculousAttachmentTypes.MIRACULOUSES) : null;
                    if (data != null) {
                        for (ResourceKey<Miraculous> type : data.getTransformed()) {
                            int powerLevel = data.get(type).powerLevel();
                            if (powerLevel > 0)
                                i *= powerLevel;
                        }
                    }
                    ServerboundTryBreakItemPayload.hurtAndBreak(stack, i, serverlevel, owner instanceof LivingEntity livingEntity ? livingEntity : null, null);
                } else if (stack.has(DataComponents.UNBREAKABLE) && owner instanceof Player player) {
                    player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
                    return;
                } else if (stack.getItem() instanceof BlockItem blockItem) {
                    float max = blockItem.getBlock().defaultDestroyTime();
                    if (max > -1) {
                        stack.set(DataComponents.MAX_DAMAGE, (int) (max * 100.0));
                        stack.set(DataComponents.DAMAGE, 0);
                        stack.set(DataComponents.MAX_STACK_SIZE, 1);
                        ServerboundTryBreakItemPayload.hurtAndBreak(stack, 100, serverlevel, owner instanceof LivingEntity livingEntity ? livingEntity : null, null);
                    } else {
                        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                        if (owner instanceof Player player) {
                            player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
                        }
                        return;
                    }
                } else if (stack.is(MineraculousItemTags.TOUGH)) {
                    stack.set(DataComponents.MAX_DAMAGE, 2);
                    stack.set(DataComponents.DAMAGE, 0);
                    stack.set(DataComponents.MAX_STACK_SIZE, 1);
                    ServerboundTryBreakItemPayload.hurtAndBreak(stack, 1, serverlevel, owner instanceof LivingEntity livingEntity ? livingEntity : null, null);
                } else {
                    if (stack.has(MineraculousDataComponents.KAMIKOTIZATION) && stack.has(DataComponents.PROFILE)) {
                        ServerPlayer target = (ServerPlayer) serverlevel.getPlayerByUUID(stack.get(DataComponents.PROFILE).gameProfile().getId());
                        if (target != null) {
                            KamikotizationData data = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                            if (data.stackCount() <= 1)
                                MineraculousEntityEvents.handleKamikotizationTransformation(target, data, false, false, position().add(0, 1, 0));
                            else {
                                data.decrementStackCount().save(target, true);
                            }
                        }
                    }
                    stack.shrink(1);
                    playSound(SoundEvents.ITEM_BREAK);
                }
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                if (!rest.isEmpty()) {
                    ItemEntity newItem = new ItemEntity(level(), getX(), getY(), getZ(), rest);
                    level().addFreshEntity(newItem);
                }
            } else if (entity.hurt(damagesource, f)) {
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

            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!target.canBeHitByProjectile() && !(target instanceof ItemEntity)) {
            return false;
        } else {
            Entity entity = this.getOwner();
            return entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(target);
        }
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    public void playerTouch(Player entity) {
        if (this.ownedBy(entity) || this.getOwner() == null) {
            super.playerTouch(entity);
        }
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        if ((this.dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!(entity.isAlive() && (!(entity instanceof ServerPlayer) || !entity.isSpectator()))) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d0)));
            }
        }

        super.tick();
    }

    @Override
    protected float getWaterInertia() {
        return 0.5F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.dealtDamage = compound.getBoolean("DealtDamage");
        setShowBlade(compound.getBoolean("ShowBlade"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("DealtDamage", this.dealtDamage);
        compound.putBoolean("ShowBlade", showBlade());
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void tickDespawn() {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> {
            if (showBlade())
                return state.setAndContinue(ButterflyCaneItem.BLADE_IDLE);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
