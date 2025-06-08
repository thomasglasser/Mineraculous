package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundOpenMiraculousTransferScreenPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
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

    public void setCharged(boolean charged) {
        entityData.set(DATA_CHARGED, charged);
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED);
    }

    public void setMiraculous(ResourceKey<Miraculous> type) {
        entityData.set(DATA_MIRACULOUS, type);
    }

    public ResourceKey<Miraculous> getMiraculous() {
        return entityData.get(DATA_MIRACULOUS);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SmoothFlyingPathNavigation(this, level);
    }

    @Override
    public float getPathfindingMalus(PathType pathType) {
        return 0;
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
        ItemStack mainHandItem = getMainHandItem();
        if (eatTicks > 0 && (isFood(mainHandItem) || isTreat(mainHandItem))) {
            eatTicks--;
            if (eatTicks <= 1) {
                if (isTreat(mainHandItem) || (isFood(mainHandItem) && random.nextInt(3) == 0)) {
                    setCharged(true);
                }
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            if (!mainHandItem.has(DataComponents.FOOD) && shouldTriggerItemUseEffects(getDefaultEatTicks(), eatTicks)) {
                this.spawnItemParticles(mainHandItem, 5);
                this.playSound(
                        this.getEatingSound(mainHandItem),
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    private boolean shouldTriggerItemUseEffects(int max, int remaining) {
        int left = max - remaining;
        int startAt = (int) ((float) max * 0.21875F);
        boolean flag = left > startAt;
        return flag && remaining % 4 == 0;
    }

    protected int getDefaultEatTicks() {
        return SharedConstants.TICKS_PER_SECOND * 3;
    }

    @Override
    public List<? extends ExtendedSensor<? extends Kwami>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>(),
                new ItemTemptingSensor<Kwami>().temptedWith((entity, stack) -> {
                    if (stack.is(MineraculousItems.MIRACULOUS)) {
                        KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                        return kwamiData != null && kwamiData.uuid().equals(entity.getUUID());
                    } else if (!entity.isCharged()) {
                        return isFood(stack) || isTreat(stack);
                    }
                    return false;
                }));
    }

    @Override
    public BrainActivityGroup<? extends Kwami> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new InvalidateAttackTarget<>() {
                    @Override
                    protected boolean canAttack(LivingEntity entity, LivingEntity target) {
                        return entity.canBeSeenByAnyone();
                    }
                }.invalidateIf(EntityUtils.TARGET_TOO_FAR_PREDICATE),
                new SetWalkTargetToAttackTarget<>(),
                new MoveToWalkTarget<>(),
                new LookAtTarget<>(),
                new AvoidEntity<>().noCloserThan(5).stopCaringAfter(10).speedModifier(2f).avoiding(livingEntity -> !livingEntity.getUUID().equals(getOwnerUUID())));
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getUUID().equals(getOwnerUUID())) {
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.isEmpty()) {
                    // TODO: Put kwami in item mode in hand
                } else {
                    KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                    if (serverPlayer.serverLevel().players().size() > 1 && stack.is(MineraculousItems.MIRACULOUS) && kwamiData != null && kwamiData.uuid().equals(getUUID())) {
                        TommyLibServices.NETWORK.sendToClient(new ClientboundOpenMiraculousTransferScreenPayload(getId()), serverPlayer);
                    } else if (!isCharged()) {
                        if (isTreat(stack) || isFood(stack)) {
                            setItemInHand(InteractionHand.MAIN_HAND, stack.copyWithCount(1));
                            FoodProperties foodProperties = stack.get(DataComponents.FOOD);
                            if (foodProperties != null) {
                                eatTicks = foodProperties.eatDurationTicks();
                                if (eatTicks <= 0)
                                    eatTicks = getDefaultEatTicks();
                            } else {
                                eatTicks = getDefaultEatTicks();
                            }
                            ItemUtils.safeShrink(1, stack, player);
                            startUsingItem(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move_controller", state -> {
            if (state.isMoving())
                return state.setAndContinue(DefaultAnimations.FLY);
            return state.setAndContinue(DefaultAnimations.IDLE);
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
        return MineraculousSoundEvents.KWAMI_HURT.get();
    }

    @Override
    public void die(DamageSource damageSource) {
        LivingEntity owner = getOwner();
        if (owner != null) {
            Predicate<ItemStack> isMyMiraculous = stack -> {
                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                return kwamiData != null && kwamiData.uuid().equals(getUUID());
            };
            List<ItemStack> inventoryMiraculouses = new ReferenceArrayList<>();
            for (ItemStack stack : EntityUtils.getInventory(owner)) {
                if (isMyMiraculous.test(stack)) {
                    inventoryMiraculouses.add(stack);
                }
            }
            Map<CuriosData, ItemStack> curiosMiraculouses = new Reference2ReferenceOpenHashMap<>();
            for (Map.Entry<CuriosData, ItemStack> entry : CuriosUtils.getAllItems(owner).entrySet()) {
                if (isMyMiraculous.test(entry.getValue())) {
                    curiosMiraculouses.put(entry.getKey(), entry.getValue());
                }
            }
            List<ItemStack> allMiraculouses = new ReferenceArrayList<>(inventoryMiraculouses);
            allMiraculouses.addAll(curiosMiraculouses.values());
            for (ItemStack stack : allMiraculouses) {
                stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
                stack.remove(MineraculousDataComponents.KWAMI_DATA);
            }
            curiosMiraculouses.forEach((data, stack) -> CuriosUtils.setStackInSlot(owner, data, stack));
        }
        super.die(damageSource);
    }

    @Override
    protected Component getTypeName() {
        if (name == null)
            name = Component.translatable(Miraculous.toLanguageKey(getMiraculous())).append(" ").append(super.getTypeName());
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
