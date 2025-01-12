package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FleeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.navigation.SmoothFlyingPathNavigation;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kwami extends TamableAnimal implements SmartBrainOwner<Kwami>, GeoEntity, FlyingAnimal {
    public static final RawAnimation EAT = RawAnimation.begin().thenPlay("misc.eat");

    private static final EntityDataAccessor<Boolean> DATA_CHARGED = SynchedEntityData.defineId(Kwami.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ResourceKey<Miraculous>> DATA_MIRACULOUS = SynchedEntityData.defineId(Kwami.class, MineraculousEntityDataSerializers.MIRACULOUS.get());

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Component name;
    private TagKey<Item> foodTag;
    private TagKey<Item> treatTag;

    public Kwami(EntityType<? extends Kwami> entityType, Level level) {
        super(entityType, level);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
        setInvulnerable(true);
        setNoGravity(true);
        noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.FLYING_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGED, true);
        builder.define(DATA_MIRACULOUS, MineraculousMiraculous.LADYBUG);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        SmoothFlyingPathNavigation nav = new SmoothFlyingPathNavigation(this, world);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public float getPathfindingMalus(PathType pathType) {
        return 0;
    }

    public void setMiraculous(ResourceKey<Miraculous> type) {
        entityData.set(DATA_MIRACULOUS, type);
    }

    public ResourceKey<Miraculous> getMiraculous() {
        return entityData.get(DATA_MIRACULOUS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Kwami>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>());
    }

    @Override
    public BrainActivityGroup<? extends Kwami> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new MoveToWalkTarget<>(),
                new LookAtTarget<>(),
                new AvoidEntity<>().noCloserThan(5).stopCaringAfter(10).speedModifier(2f).avoiding(livingEntity -> livingEntity instanceof Player && livingEntity != getOwner()),
                new FleeTarget<>().speedModifier(1.5f));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kwami> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()),
                new FirstApplicableBehaviour<>(
                        // TODO: Remove following when SBL updated to fix changing owners
                        new FollowOwner<>().following(OwnableEntity::getOwner).speedMod(10f).stopFollowingWithin(4).teleportToTargetAfter(10).startCondition(kwami -> kwami.getOwner() != null),
                        new OneRandomBehaviour<>(
                                new SetRandomFlyingTarget<>(),
                                new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60)))));
    }

    public void setCharged(boolean charged) {
        entityData.set(DATA_CHARGED, charged);
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide && player == getOwner()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!isCharged()) {
                if (isTreat(stack) || (isFood(stack) && random.nextInt(3) == 0)) {
                    playHurtSound(level().damageSources().starve());
                    setCharged(true);
                }
                if (isTreat(stack) || isFood(stack)) {
                    ItemStack remainder = ItemUtils.safeShrink(1, stack, player);
                    triggerAnim("eat_controller", "eat");
                    if (!remainder.isEmpty()) player.addItem(remainder);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, player);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move_controller", state -> {
            if (state.isMoving())
                return state.setAndContinue(DefaultAnimations.FLY);
            return state.setAndContinue(DefaultAnimations.IDLE);
        }));
        controllers.add(new AnimationController<>(this, "eat_controller", state -> PlayState.STOP)
                .triggerableAnim("eat", EAT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean isFood(ItemStack stack) {
        if (getMiraculous() != null) {
            if (foodTag == null)
                foodTag = Miraculous.createFoodsTag(getMiraculous());
            return stack.is(foodTag);
        }
        return false;
    }

    public boolean isTreat(ItemStack stack) {
        if (getMiraculous() != null) {
            if (treatTag == null)
                treatTag = Miraculous.createTreatsTag(getMiraculous());
            return stack.is(treatTag);
        }
        return false;
    }

    @Override
    public void playHurtSound(DamageSource source) {
        super.playHurtSound(source);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.7F;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        if (damageSource.is(DamageTypes.STARVE))
            return MineraculousSoundEvents.KWAMI_HUNGRY.get();
        return super.getHurtSound(damageSource);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (getOwner() instanceof ServerPlayer player) {
            Predicate<ItemStack> isMyJewel = stack -> stack.has(MineraculousDataComponents.KWAMI_DATA.get()) && stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(getUUID());
            List<ItemStack> miraculous = new ArrayList<>(player.getInventory().items.stream().filter(isMyJewel).toList());
            Map<CuriosData, ItemStack> allCurios = CuriosUtils.getAllItems(player);
            Map<CuriosData, ItemStack> curios = new HashMap<>();
            allCurios.forEach(((curiosData, stack) -> {
                if (isMyJewel.test(stack))
                    curios.put(curiosData, stack);
            }));
            List<ItemStack> all = new ArrayList<>(miraculous);
            all.addAll(curios.values());
            for (ItemStack stack : all) {
                stack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                stack.remove(MineraculousDataComponents.KWAMI_DATA.get());
            }
            curios.forEach((data, stack) -> CuriosUtils.setStackInSlot(player, data, stack, true));
        }
        super.die(damageSource);
    }

    @Override
    protected Component getTypeName() {
        if (name == null)
            name = Component.translatable(MineraculousEntityTypes.KWAMI.getId().toLanguageKey("entity", getMiraculous().location().getPath()));
        return name;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Charged", isCharged());
        compound.put("Miraculous", ResourceKey.codec(MineraculousRegistries.MIRACULOUS).encodeStart(NbtOps.INSTANCE, getMiraculous()).getOrThrow());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setCharged(compound.getBoolean("Charged"));
        setMiraculous(ResourceKey.codec(MineraculousRegistries.MIRACULOUS).parse(NbtOps.INSTANCE, compound.get("Miraculous")).getOrThrow());
    }

    @Override
    public boolean isFlying() {
        return true;
    }
}
