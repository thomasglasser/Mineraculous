package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundOpenMiraculousTransferScreenPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.navigation.SmoothFlyingPathNavigation;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kwami extends ShoulderRidingEntity implements SmartBrainOwner<Kwami>, GeoEntity, FlyingAnimal {
    public static final RawAnimation EAT = RawAnimation.begin().thenPlay("misc.eat");
    public static final RawAnimation HOLD = RawAnimation.begin().thenPlay("misc.hold");
    public static final RawAnimation SIT = RawAnimation.begin().thenPlay("misc.sit");

    private static final EntityDataAccessor<Boolean> DATA_CHARGED = SynchedEntityData.defineId(Kwami.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ResourceKey<Miraculous>> DATA_MIRACULOUS = SynchedEntityData.defineId(Kwami.class, MineraculousEntityDataSerializers.MIRACULOUS.get());

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Component name;
    private TagKey<Item> foodTag;
    private TagKey<Item> treatTag;

    private int eatTicks = 0;
    private boolean onShoulder = false;

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
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 1024);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGED, true);
        builder.define(DATA_MIRACULOUS, Miraculouses.LADYBUG);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new SmoothFlyingPathNavigation(this, world);
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
        if (isFood(getMainHandItem()) || isTreat(getMainHandItem())) {
            eatTicks--;
            if (eatTicks <= 0) {
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                setCharged(true);
            }
        }
    }

    @Override
    public List<? extends ExtendedSensor<? extends Kwami>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>(),
                new ItemTemptingSensor<Kwami>().temptedWith((entity, stack) -> (!isCharged() && (isFood(stack) || isTreat(stack))) || (stack.is(MineraculousItems.MIRACULOUS) && stack.has(MineraculousDataComponents.KWAMI_DATA) && stack.get(MineraculousDataComponents.KWAMI_DATA).uuid().equals(getUUID()))));
    }

    @Override
    public BrainActivityGroup<? extends Kwami> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new InvalidateAttackTarget<>() {
                    @Override
                    protected boolean canAttack(LivingEntity entity, LivingEntity target) {
                        return entity.canBeSeenByAnyone();
                    }
                }.invalidateIf(Kamiko.TARGET_TOO_FAR),
                new SetWalkTargetToAttackTarget<>(),
                new MoveToWalkTarget<>(),
                new LookAtTarget<>(),
                new AvoidEntity<>().noCloserThan(5).stopCaringAfter(10).speedModifier(2f).avoiding(livingEntity -> getOwner() != null && livingEntity instanceof Player && livingEntity != getOwner()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kwami> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()),
                new FirstApplicableBehaviour<>(
                        new FollowOwner<Kwami>().speedMod(10f).stopFollowingWithin(2).teleportToTargetAfter(8).startCondition(kwami -> kwami.getOwner() != null),
                        new FollowTemptation<>(),
                        new OneRandomBehaviour<>(
                                new SetRandomFlyingTarget<>().flightTargetPredicate((entity, pos) -> pos != null && entity.level().getBlockState(BlockPos.containing(pos)).isAir()),
                                new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60)))));
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

    public void setCharged(boolean charged) {
        entityData.set(DATA_CHARGED, charged);
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player == getOwner()) {
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.isEmpty()) {
                    return setEntityOnShoulder(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                } else if (serverPlayer.serverLevel().players().size() > 1 && stack.is(MineraculousItems.MIRACULOUS) && stack.has(MineraculousDataComponents.KWAMI_DATA) && stack.get(MineraculousDataComponents.KWAMI_DATA).uuid().equals(getUUID())) {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundOpenMiraculousTransferScreenPayload(getId()), serverPlayer);
                    return InteractionResult.SUCCESS;
                }
                if (!isCharged()) {
                    if (isTreat(stack) || (isFood(stack) && random.nextInt(3) == 0)) {
                        playHurtSound(level().damageSources().starve());
                        setItemInHand(hand, stack.copyWithCount(1));
                        FoodProperties foodData = stack.get(DataComponents.FOOD);
                        if (foodData != null) {
                            eatTicks = foodData.eatDurationTicks() * 2;
                        } else {
                            eatTicks = 40;
                        }
                    }
                    if (isTreat(stack) || isFood(stack)) {
                        ItemUtils.safeShrink(1, stack, player);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move_controller", state -> {
            if (state.isMoving())
                return state.setAndContinue(DefaultAnimations.FLY);
            return onShoulder ? state.setAndContinue(SIT) : state.setAndContinue(DefaultAnimations.IDLE);
        }));
        controllers.add(new AnimationController<>(this, "item_controller", state -> {
            ItemStack mainHandItem = this.getMainHandItem();
            if (isFood(mainHandItem) || isTreat(mainHandItem)) {
                return state.setAndContinue(EAT);
            } else if (!mainHandItem.isEmpty()) {
                return state.setAndContinue(HOLD);
            }
            return PlayState.STOP;
        }));
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
            curios.forEach((data, stack) -> CuriosUtils.setStackInSlot(player, data, stack));
        }
        super.die(damageSource);
    }

    @Override
    protected Component getTypeName() {
        if (name == null)
            name = Component.translatable(MineraculousEntityTypes.KWAMI.getId().toLanguageKey("entity", getMiraculous().location().toShortLanguageKey()));
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

    public void setOnShoulder() {
        onShoulder = true;
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (getTarget() == player && getOwner() == null) {
            setTarget(null);
            setOwnerUUID(player.getUUID());
            ItemStack stack = this.getMainHandItem();
            stack.remove(MineraculousDataComponents.POWERED);
            player.addItem(stack);
        }
    }
}
