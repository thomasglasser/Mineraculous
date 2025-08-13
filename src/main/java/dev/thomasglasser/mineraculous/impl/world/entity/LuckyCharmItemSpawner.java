package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class LuckyCharmItemSpawner extends Entity {
    private static final String TAG_SPAWN_ITEM_AFTER_TICKS = "spawn_item_after_ticks";
    private static final String TAG_ITEM = "item";
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(LuckyCharmItemSpawner.class, EntityDataSerializers.ITEM_STACK);
    public static final int TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND = 36;
    private int spawnItemAfterTicks;

    public LuckyCharmItemSpawner(EntityType<? extends LuckyCharmItemSpawner> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public static LuckyCharmItemSpawner create(ServerLevel level, ItemStack item) {
        LuckyCharmItemSpawner spawner = new LuckyCharmItemSpawner(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), level);
        spawner.spawnItemAfterTicks = level.random.nextIntBetweenInclusive(MineraculousServerConfig.get().luckyCharmSummonTimeMin.get() * SharedConstants.TICKS_PER_SECOND, MineraculousServerConfig.get().luckyCharmSummonTimeMax.get() * SharedConstants.TICKS_PER_SECOND);
        spawner.setItem(item);
        return spawner;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClient();
        } else {
            this.tickServer();
        }
    }

    private void tickServer() {
        if (this.tickCount == this.spawnItemAfterTicks - TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundSource.PLAYERS);
        }

        if (this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill();
        }
    }

    private void tickClient() {
        this.addParticles();
    }

    private void spawnItem() {
        Level level = this.level();
        ItemStack itemstack = this.getItem();
        if (!itemstack.isEmpty()) {
            List<Entity> entities = new ReferenceArrayList<>();
            if (itemstack.getItem() instanceof ProjectileItem projectileItem) {
                for (int i = 0; i < itemstack.getCount(); i++) {
                    Direction direction = Direction.DOWN;
                    Projectile projectile = projectileItem.asProjectile(level, this.position(), itemstack, direction);
                    projectile.setOwner(this);
                    ProjectileItem.DispenseConfig dispenseConfig = projectileItem.createDispenseConfig();
                    projectileItem.shoot(
                            projectile,
                            direction.getStepX(),
                            direction.getStepY(),
                            direction.getStepZ(),
                            dispenseConfig.power(),
                            dispenseConfig.uncertainty());
                    dispenseConfig.overrideDispenseEvent().ifPresent(p_352709_ -> level.levelEvent(p_352709_, this.blockPosition(), 0));
                    entities.add(projectile);
                }
            } else {
                entities.add(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), itemstack));
            }

            for (Entity entity : entities) {
                level.addFreshEntity(entity);
                level.gameEvent(entity, GameEvent.ENTITY_PLACE, this.position());
            }
            this.setItem(ItemStack.EMPTY);
        }
    }

    public void addParticles() {
        Vec3 vec3 = this.position();
        int i = this.random.nextIntBetweenInclusive(1, 3);

        for (int j = 0; j < i; j++) {
            double d0 = 0.4;
            Vec3 vec31 = new Vec3(
                    this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()),
                    this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()),
                    this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3 vec32 = vec3.vectorTo(vec31);
            this.level().addParticle(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), vec3.x(), vec3.y(), vec3.z(), vec32.x(), vec32.y(), vec32.z());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    private void setItem(ItemStack item) {
        this.getEntityData().set(DATA_ITEM, item);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setItem(ItemStack.parseOptional(registryAccess(), compound.getCompound(TAG_ITEM)));
        this.spawnItemAfterTicks = compound.getInt(TAG_SPAWN_ITEM_AFTER_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (!this.getItem().isEmpty()) {
            compound.put(TAG_ITEM, this.getItem().save(this.registryAccess()).copy());
        }

        compound.putInt(TAG_SPAWN_ITEM_AFTER_TICKS, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
