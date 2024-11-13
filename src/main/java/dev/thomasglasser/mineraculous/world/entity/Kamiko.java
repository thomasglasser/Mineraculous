package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ClientboundOpenKamikotizationSelectionScreenPayload;
import dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko.RestBehaviour;
import dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko.move.SetRandomRestTarget;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kamiko extends TamableAnimal implements SmartBrainOwner<Kamiko>, GeoEntity {
    public static final ResourceLocation SPECTATOR_SHADER = Mineraculous.modLoc("kamiko");

    private static final EntityDataAccessor<Boolean> RESTING = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    public Kamiko(EntityType<? extends Kamiko> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 180, true);
        setNoGravity(true);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RESTING, false);
    }

    public boolean isPowered() {
        return getOwnerUUID() != null;
    }

    public boolean isResting() {
        return entityData.get(RESTING);
    }

    public void setResting(boolean resting) {
        entityData.set(RESTING, resting);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    // No Push
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {}

    @Override
    protected void pushEntities() {}

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isResting()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel p_376725_) {
        super.customServerAiStep(p_376725_);
        tickBrain(this);
    }

    // Flying mobs don't take fall damage.
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (isPowered())
            return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        return source.is(DamageTypes.IN_WALL) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !this.level().isClientSide && this.isResting()) {
            this.setResting(false);
        }
        return hurt;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(RESTING, tag.getBoolean("Resting"));
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Resting", this.entityData.get(RESTING));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1) // Butterflies are weak, okay.
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 1)
                .add(Attributes.GRAVITY, 0)
                .add(Attributes.FOLLOW_RANGE, 2048);
    }

    // AI
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Kamiko>> getSensors() {
        return List.of();
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new SetWalkTargetToAttackTarget<Kamiko>()
                        .whenStarting(mob -> mob.setResting(false)),
                new MoveToWalkTarget<Kamiko>()
                        .startCondition(kamiko -> !kamiko.isResting()),
                new RestBehaviour<>().runFor(kamiko -> kamiko.random.nextInt(60, 200)).cooldownFor(kamiko -> 600));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new OneRandomBehaviour<>(
                        new SetRandomFlyingTarget<>(),
                        new SetRandomRestTarget<Kamiko>()
                                .setRadius(10, 64)
                                .startCondition(kamiko -> !kamiko.isResting() && kamiko.getTarget() == null)));
    }

    // ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animations));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    private <T extends Kamiko> PlayState animations(AnimationState<T> animationState) {
        if (animationState.getAnimatable().isResting()) {
            return animationState.setAndContinue(DefaultAnimations.IDLE);
        }
        return animationState.setAndContinue(DefaultAnimations.FLY);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(super.getTarget());
    }

    @Override
    public void playerTouch(Player player) {
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner) {
            CompoundTag ownerData = TommyLibServices.ENTITY.getPersistentData(owner);
            ownerData.putBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, true);
            TommyLibServices.ENTITY.setPersistentData(getOwner(), ownerData, true);
            remove(RemovalReason.DISCARDED);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenKamikotizationSelectionScreenPayload(player.getUUID(), new KamikoData(getUUID(), getOwnerUUID())), owner);
        }
    }
}
