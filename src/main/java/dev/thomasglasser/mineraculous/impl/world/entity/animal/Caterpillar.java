package dev.thomasglasser.mineraculous.impl.world.entity.animal;

import com.nyfaria.awcapi.ClimberHelper;
import com.nyfaria.awcapi.entity.ClimberComponent;
import com.nyfaria.awcapi.entity.IAdvancedClimber;
import com.nyfaria.awcapi.entity.movement.ClimberPathNavigator;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.animal.ButterflyVariant;
import dev.thomasglasser.mineraculous.api.world.entity.animal.ButterflyVariants;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Caterpillar extends Animal implements SmartBrainOwner<Caterpillar>, GeoEntity, IAdvancedClimber {
    private static final EntityDataAccessor<Holder<ButterflyVariant>> DATA_VARIANT = SynchedEntityData.defineId(Caterpillar.class, MineraculousEntityDataSerializers.BUTTERFLY_VARIANT.get());

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ClimberComponent climberComponent;

    public Caterpillar(EntityType<? extends Caterpillar> entityType, Level level) {
        super(entityType, level);
        this.climberComponent = new ClimberComponent(this);
        ClimberHelper.initClimber(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.SAFE_FALL_DISTANCE, 64);
    }

    @Override
    public ClimberComponent getClimberComponent() {
        return climberComponent;
    }

    @Override
    public Mob asMob() {
        return this;
    }

    @Override
    public void setLerpYRot(Float f) {
        this.lerpYRot = f;
    }

    @Override
    public void setLerpXRot(Float f) {
        this.lerpXRot = f;
    }

    @Override
    public void setLerpYHeadRot(Float f) {
        this.lerpYHeadRot = f;
    }

    @Override
    public void setLerpHeadSteps(int i) {
        this.lerpHeadSteps = i;
    }

    @Override
    public float getMovementSpeed() {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public float getBlockSlipperiness(BlockPos pos) {
        return level().getBlockState(pos).getBlock().getFriction() * 0.91f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        Registry<ButterflyVariant> registry = this.registryAccess().registryOrThrow(MineraculousRegistries.BUTTERFLY_VARIANT);
        builder.define(DATA_VARIANT, registry.getHolder(ButterflyVariants.TEMPERATE).or(registry::getAny).orElseThrow());
    }

    public Holder<ButterflyVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(Holder<ButterflyVariant> variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        ClimberPathNavigator<Caterpillar> navigator = new ClimberPathNavigator<>(this, level, false);
        navigator.setCanFloat(true);
        return navigator;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MineraculousItemTags.CATERPILLAR_FOOD);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(ButterflyVariants.getSpawnVariant(this.registryAccess(), level.getBiome(this.blockPosition())));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {}

    @Override
    public void aiStep() {
        ClimberHelper.livingTickClimber(this);
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        ClimberHelper.tickClimber(this);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        ClimberHelper.handleMove(this, type, movement, true);
        super.move(type, movement);
        ClimberHelper.handleMove(this, type, movement, false);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!ClimberHelper.handleTravel(this, travelVector)) {
            super.travel(travelVector);
        }
        ClimberHelper.postTravel(this, travelVector);
    }

    @Override
    public void jumpFromGround() {
        if (!ClimberHelper.handleJump(this)) {
            super.jumpFromGround();
        }
    }

    @Override
    public BlockPos getOnPos() {
        return ClimberHelper.getAdjustedOnPosition(this, super.getOnPos());
    }

    @Override
    public boolean onClimbable() {
        return false; // Disable vanilla climbing
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Caterpillar>> getSensors() {
        return ObjectArrayList.of(
                new ItemTemptingSensor<Caterpillar>().temptedWith((caterpillar, stack) -> stack.is(MineraculousItemTags.CATERPILLAR_FOOD)));
    }

    @Override
    public BrainActivityGroup<? extends Caterpillar> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends Caterpillar> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new FollowTemptation<>(),
                        new OneRandomBehaviour<>(
                                new SetRandomWalkTarget<>(),
                                new Idle<>())));
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

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
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state -> {
            if (state.isMoving())
                return state.setAndContinue(DefaultAnimations.WALK);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.getVariant().unwrapKey().ifPresent(key -> compound.putString("variant", key.location().toString()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString("variant")))
                .map(loc -> ResourceKey.create(MineraculousRegistries.BUTTERFLY_VARIANT, loc))
                .flatMap(key -> this.registryAccess().registryOrThrow(MineraculousRegistries.BUTTERFLY_VARIANT).getHolder(key))
                .ifPresent(this::setVariant);
    }
}
