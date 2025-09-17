package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.ai.behavior.SetWalkTargetToLikedPlayer;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.network.ClientboundOpenMiraculousTransferScreenPayload;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.navigation.SmoothFlyingPathNavigation;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
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
    public static final RawAnimation HOLD = RawAnimation.begin().thenPlay("misc.hold");

    public static final BiPredicate<Kwami, Player> RENOUNCE_PREDICATE = (kwami, player) -> player != null && player.isAlive() && player != kwami.getOwner() && EntitySelector.NO_SPECTATORS.test(player) && !EntityUtils.TARGET_TOO_FAR_PREDICATE.test(kwami, player);

    private static final double SUMMON_RADIUS_STEP = 0.07;
    private static final double SUMMON_ANGLE_STEP = 0.5;
    private static final double SUMMON_MAX_RADIUS = 1.5;

    private static final EntityDataAccessor<Integer> DATA_SUMMON_TICKS = SynchedEntityData.defineId(Kwami.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGED = SynchedEntityData.defineId(Kwami.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Holder<Miraculous>> DATA_MIRACULOUS = SynchedEntityData.defineId(Kwami.class, MineraculousEntityDataSerializers.MIRACULOUS.get());
    private static final EntityDataAccessor<UUID> DATA_MIRACULOUS_ID = SynchedEntityData.defineId(Kwami.class, MineraculousEntityDataSerializers.UUID.get());
    private static final EntityDataAccessor<Boolean> DATA_TRANSFORMING = SynchedEntityData.defineId(Kwami.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Component name;
    private TagKey<Item> foodTag;
    private TagKey<Item> treatTag;

    private double summonAngle;
    private double summonRadius;
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
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 1024);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUMMON_TICKS, 0);
        builder.define(DATA_CHARGED, true);
        builder.define(DATA_MIRACULOUS, level().holderOrThrow(Miraculouses.LADYBUG));
        builder.define(DATA_MIRACULOUS_ID, Util.NIL_UUID);
        builder.define(DATA_TRANSFORMING, false);
    }

    public int getSummonTicks() {
        return entityData.get(DATA_SUMMON_TICKS);
    }

    public void setSummonTicks(int ticks) {
        entityData.set(DATA_SUMMON_TICKS, ticks);
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED);
    }

    public void setCharged(boolean charged) {
        entityData.set(DATA_CHARGED, charged);
    }

    public Holder<Miraculous> getMiraculous() {
        return entityData.get(DATA_MIRACULOUS);
    }

    public void setMiraculous(Holder<Miraculous> type) {
        entityData.set(DATA_MIRACULOUS, type);
    }

    public UUID getMiraculousId() {
        return entityData.get(DATA_MIRACULOUS_ID);
    }

    public void setMiraculousId(UUID id) {
        entityData.set(DATA_MIRACULOUS_ID, id);
    }

    public boolean isTransforming() {
        return entityData.get(DATA_TRANSFORMING);
    }

    public void setTransforming(boolean transforming) {
        entityData.set(DATA_TRANSFORMING, transforming);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        SmoothFlyingPathNavigation navigation = new SmoothFlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
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

    private void tickSummon() {
        setSummonTicks(getSummonTicks() - 1);
        LivingEntity owner = getOwner();
        if (owner != null) {
            Vec3 ownerEyePos = owner.getEyePosition();

            Vec3 lookVec = ownerEyePos.subtract(this.position());
            float yaw = (float) (Math.toDegrees(Math.atan2(lookVec.z, lookVec.x)) - 90.0F);
            float pitch = (float) (-Math.toDegrees(Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z))));

            if (this.getY() < ownerEyePos.y - this.getBbHeight() / 2) {
                summonRadius += SUMMON_RADIUS_STEP;
                summonAngle += SUMMON_ANGLE_STEP;
                if (summonRadius > SUMMON_MAX_RADIUS) {
                    summonRadius = SUMMON_MAX_RADIUS;
                }

                Vec3 ownerPos = owner.position();
                double startY = ownerPos.y + owner.getBbHeight() / 2;
                double t = Math.min(summonRadius / SUMMON_MAX_RADIUS, 1.0);
                double y = startY + (ownerEyePos.y - startY) * t - this.getBbHeight() / 2;

                float baseYawRad = (float) Math.toRadians(180 + owner.getYRot());
                double x = ownerPos.x + summonRadius * Math.cos(summonAngle + baseYawRad);
                double z = ownerPos.z + summonRadius * Math.sin(summonAngle + baseYawRad);

                this.moveTo(x, y, z, yaw, pitch);
            }
        }
    }

    private void tickTransforming() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            setDeltaMovement(owner.getEyePosition().subtract(this.getEyePosition()).scale(0.4));
            if (this.getEyePosition().closerThan(owner.getEyePosition(), 0.1)) {
                setTransforming(false);
                discard();
            }
        } else {
            setTransforming(false);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (getSummonTicks() > 0) {
            tickSummon();
            return;
        } else if (isTransforming()) {
            tickTransforming();
            return;
        }

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
                this.playSound(
                        this.getEatingSound(mainHandItem),
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        } else if (!mainHandItem.isEmpty()) {
            UUID likedId = BrainUtils.getMemory(this, MemoryModuleType.LIKED_PLAYER);
            if (likedId == null) {
                Player target = findRandomRenounceTarget();
                if (target == null && getOwner() instanceof Player player) {
                    target = player;
                }
                if (target != null) {
                    BrainUtils.setMemory(this, MemoryModuleType.LIKED_PLAYER, target.getUUID());
                }
            } else if (!RENOUNCE_PREDICATE.test(this, level().getPlayerByUUID(likedId))) {
                BrainUtils.setMemory(this, MemoryModuleType.LIKED_PLAYER, null);
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
                new ItemTemptingSensor<Kwami>().temptedWith((kwami, stack) -> {
                    if (stack.getItem() instanceof MiraculousItem) {
                        UUID stackId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
                        return stackId != null && stackId.equals(kwami.getMiraculousId());
                    } else if (!kwami.isCharged()) {
                        return isFood(stack) || isTreat(stack);
                    }
                    return false;
                }));
    }

    @Override
    public BrainActivityGroup<? extends Kwami> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new FloatToSurfaceOfFluid<>(),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends Kwami> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()),
                new FirstApplicableBehaviour<>(
                        new SetWalkTargetToLikedPlayer<>(),
                        new FollowTemptation<>(),
                        new OneRandomBehaviour<>(
                                new FollowOwner<>().speedMod(10).stopFollowingWithin(2).teleportToTargetAfter(8),
                                new SetRandomFlyingTarget<>().flightTargetPredicate((entity, pos) -> pos != null && entity.level().getBlockState(BlockPos.containing(pos)).isAir()),
                                new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60)))));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getUUID().equals(getOwnerUUID())) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    UUID stackId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
                    if (!serverPlayer.serverLevel().getPlayers(p -> RENOUNCE_PREDICATE.test(this, p)).isEmpty() && stack.getItem() instanceof MiraculousItem && stackId != null && stackId.equals(getMiraculousId())) {
                        TommyLibServices.NETWORK.sendToClient(new ClientboundOpenMiraculousTransferScreenPayload(getId()), serverPlayer);
                    } else if (!isCharged() && getMainHandItem().isEmpty()) {
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
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
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
                foodTag = Miraculous.createFoodsTag(getMiraculous().getKey());
            return stack.is(foodTag);
        }
        return false;
    }

    public boolean isTreat(ItemStack stack) {
        if (getMiraculous() != null) {
            if (treatTag == null)
                treatTag = Miraculous.createTreatsTag(getMiraculous().getKey());
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
                UUID stackId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
                return stackId != null && stackId.equals(getMiraculousId());
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
            }
            curiosMiraculouses.forEach((data, stack) -> CuriosUtils.setStackInSlot(owner, data, stack));
        }
        super.die(damageSource);
    }

    @Override
    protected Component getTypeName() {
        if (name == null)
            name = Component.translatable(Miraculous.toLanguageKey(getMiraculous().getKey())).append(" ").append(super.getTypeName());
        return name;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SummonTicks", getSummonTicks());
        compound.putBoolean("Charged", isCharged());
        compound.put("Miraculous", Miraculous.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), getMiraculous()).getOrThrow());
        compound.putUUID("MiraculousId", getMiraculousId());
        compound.putInt("EatTicks", eatTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setSummonTicks(compound.getInt("SummonTicks"));
        setCharged(compound.getBoolean("Charged"));
        setMiraculous(Miraculous.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compound.get("Miraculous")).getOrThrow());
        setMiraculousId(compound.getUUID("MiraculousId"));
        eatTicks = compound.getInt("EatTicks");
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (player.getUUID().equals(BrainUtils.getMemory(this, MemoryModuleType.LIKED_PLAYER)) && getOwner() == null) {
            BrainUtils.setMemory(this, MemoryModuleType.LIKED_PLAYER, null);
            setOwnerUUID(player.getUUID());
            getMainHandItem().remove(MineraculousDataComponents.POWERED);
            player.addItem(getMainHandItem());
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    public @Nullable Player findRandomRenounceTarget() {
        if (level() instanceof ServerLevel level) {
            List<ServerPlayer> players = level.getPlayers(p -> RENOUNCE_PREDICATE.test(this, p));
            return players.get(level.random.nextInt(players.size()));
        }
        throw new IllegalStateException("Cannot pick random renounce target from the client.");
    }
}
