package dev.thomasglasser.mineraculous.world.entity.projectile;

import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.network.ClientboundSyncLadybugYoyoPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownLadybugYoyo extends AbstractArrow implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private LadybugYoyoItem.Ability ability;
    private boolean dealtDamage;
    public double maxRopeLength = 0; // used for rendering the rope
    public double serverMaxRopeLength = 0; // used for swinging physics

    public ThrownLadybugYoyo(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable LadybugYoyoItem.Ability ability) {
        super(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), owner, level, pickupItemStack, null);
        setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        this.ability = ability;
        owner.setData(MineraculousAttachmentTypes.LADYBUG_YOYO, Optional.of(this.getUUID()));
        if (level instanceof ServerLevel serverLevel) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncLadybugYoyoPayload(Optional.of(this.getUUID())), serverLevel.getServer());
        }
    }

    public ThrownLadybugYoyo(double x, double y, double z, Level level, ItemStack pickupItemStack, @Nullable LadybugYoyoItem.Ability ability) {
        super(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), x, y, z, level, pickupItemStack, null);
        this.ability = ability;
    }

    public ThrownLadybugYoyo(EntityType<? extends ThrownLadybugYoyo> entityType, Level level) {
        super(entityType, level);
        this.ability = null;
        this.noCulling = true;
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof Player ? (Player) entity : null;
    }

    public boolean inGround() {
        return this.inGround;
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        super.tick();
        Entity entity = getOwner();
        if (entity == null)
            return;
        if (entity instanceof Player player) {
            checkRecall(player);
            if (this.inGround()) {
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - this.getX(), player.getY() - this.getY(), player.getZ() - this.getZ());
                double distance = fromProjectileToPlayer.length();
                if (distance > this.serverMaxRopeLength) {

                    Vec3 constrainedPosition = player.position()
                            .add(fromProjectileToPlayer.normalize().scale(serverMaxRopeLength - distance));
                    normalCollisions(false, player);
                    player.setPos(constrainedPosition.x, constrainedPosition.y, constrainedPosition.z);

                    Vec3 radialForce = fromProjectileToPlayer.normalize();
                    Vec3 tangentialVelocity = player.getDeltaMovement().subtract(
                            radialForce.scale(player.getDeltaMovement().dot(radialForce)));
                    double dampingFactor = Math.max(1.06, 1 - Math.abs(distance - serverMaxRopeLength) * 0.02); // Less damping near center
                    Vec3 dampedVelocity = tangentialVelocity.scale(dampingFactor);

                    Vec3 correctiveForce = radialForce.scale((distance - serverMaxRopeLength) * 0.005);
                    Vec3 newVelocity = dampedVelocity.add(correctiveForce);

                    if (this.getY() > player.getY()) {
                        player.setDeltaMovement(newVelocity);
                    }

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
        Entity entity = result.getEntity();
        float f = 8.0F;
        Entity entity1 = this.getOwner();
        DamageSource damagesource = this.damageSources().trident(this, entity1 == null ? this : entity1);
        if (this.level() instanceof ServerLevel serverlevel) {
            f = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, f);
        }

        this.dealtDamage = true;
        if (this.ability == null) {
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
        }
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
        super.onHitBlock(result);
        if (ability != LadybugYoyoItem.Ability.TRAVEL) {
            //discard();
        }
        //TODO if the ability is travel
        Player p = this.getPlayerOwner();
        if (p != null && this.inGround) {
            Vec3 vec3 = ThrownLadybugYoyoRenderer.getPlayerHandPos(p, 1, MineraculousItems.LADYBUG_YOYO.get(), Minecraft.getInstance().getEntityRenderDispatcher());
            Vec3 fromProjectileToHand = new Vec3(vec3.x - this.getX(), vec3.y - this.getY(), vec3.z - this.getZ());
            this.maxRopeLength = fromProjectileToHand.length();

            Vec3 fromProjectileToPlayer = new Vec3(p.getX() - this.getX(), p.getY() - this.getY(), p.getZ() - this.getZ());
            this.serverMaxRopeLength = fromProjectileToPlayer.length() + 1;
        }
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
            this.ability = LadybugYoyoItem.Ability.valueOf(compound.getString("Ability"));
        this.dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ability != null)
            compound.putString("Ability", this.ability.name());
        compound.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> state.setAndContinue(DefaultAnimations.IDLE)));
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
