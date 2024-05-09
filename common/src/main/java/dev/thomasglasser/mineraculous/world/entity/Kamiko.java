package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko.RestBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrain;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class Kamiko extends PathfinderMob implements SmartBrainOwner<Kamiko>, GeoEntity {
    private static final EntityDataAccessor<Boolean> RESTING = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);
    private BlockPos followingPosition = null;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    public Kamiko(EntityType<? extends Kamiko> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 180, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RESTING, false);
        builder.define(POWERED, false);
    }

    public BlockPos getFollowingPosition() {
        return followingPosition;
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

    @Override // Butterflies can't be leashed.
    public boolean canBeLeashed(Player player) {
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
    protected void customServerAiStep() {
        if (this.followingPosition==null) this.followingPosition = this.blockPosition();
        tickBrain(this);
    }

    @Override // Kamikos are unconstrained by gravity.
    protected double getDefaultGravity() {
        return 0;
    }

    // Flying mobs don't take fall damage.
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Kamikos can squeeze through impossibly tight spaces. Including between atomic particles.
        return source.is(DamageTypes.IN_WALL) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isResting()) {
            this.setResting(false);
        }
        return hurt;
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
                .add(Attributes.FLYING_SPEED, 1);
    }

    // AI
    @Override
    protected PathNavigation createNavigation(@NotNull Level level) {
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
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kamiko> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new MoveToWalkTarget<>()
                        .startCondition(kamiko -> ((SmartBrain<Kamiko>)kamiko.getBrain()).getBehaviours().filter(b -> b instanceof RestBehaviour).map(b->(RestBehaviour<Kamiko>)b).findFirst().get().timedOut(kamiko.level().getGameTime())) // Long line of logic that is impossible to read.
                        .stopIf(kamiko -> kamiko.getTarget()!=null),
                new RestBehaviour<>().runFor(kamiko -> kamiko.random.nextInt(60,200)).cooldownFor(kamiko -> 600)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new OneRandomBehaviour<>(
                        new SetRandomFlyingTarget<>(),
                        new SetRandomWalkTarget<Kamiko>()
                                .setRadius(10,64)
                                .startCondition(kamiko -> !kamiko.isResting())
                )
        );
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new FollowEntity<Kamiko,LivingEntity>()
                        .following(Mob::getTarget)
                        .stopFollowingWithin(0)
                        .speedMod(3)
                        .startCondition(kamiko -> (kamiko.getTarget()!=null) && (kamiko.isPowered()))
                        .noTimeout().stopIf(kamiko -> (kamiko.getTarget() == null) || (kamiko.getTarget().isRemoved()))
        );
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
            animationState.getController().setAnimationSpeed(1);
            return animationState.setAndContinue(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }

        animationState.getController().setAnimationSpeed(4);
        return animationState.setAndContinue(RawAnimation.begin().then("flying", Animation.LoopType.LOOP));
    }
}
