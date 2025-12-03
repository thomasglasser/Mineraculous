package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.SpectateEntityAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.ai.behavior.Replicate;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.ReplicationState;
import dev.thomasglasser.mineraculous.api.world.entity.ai.sensing.PlayerItemTemptingSensor;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundBeginKamikotizationSelectionPayload;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomHoverTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.navigation.SmoothFlyingPathNavigation;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kamiko extends TamableAnimal implements SmartBrainOwner<Kamiko>, GeoEntity {
    public static final ResourceLocation SPECTATOR_SHADER = MineraculousConstants.modLoc("shaders/post/kamiko.json");
    public static final Component DETRANSFORM_TO_TRANSFORM = Component.translatable("entity.mineraculous.kamiko.detransform_to_transform");
    public static final Component CANT_KAMIKOTIZE_TRANSFORMED = Component.translatable("entity.mineraculous.kamiko.cant_kamikotize_transformed");

    private static final EntityDataAccessor<Integer> DATA_POWER_LEVEL = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_NAME_COLOR = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<ResourceLocation>> DATA_FACE_MASK_TEXTURE = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_RESOURCE_LOCATION.get());
    private static final EntityDataAccessor<Optional<Holder<Kamikotization>>> DATA_KAMIKOTIZATION = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_KAMIKOTIZATION.get());
    private static final EntityDataAccessor<Optional<UUID>> DATA_REPLICA_SOURCE = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_IS_REPLICA = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_RESTING = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Kamiko(EntityType<? extends Kamiko> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 180, true);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_POWER_LEVEL, 0);
        builder.define(DATA_NAME_COLOR, -1);
        builder.define(DATA_FACE_MASK_TEXTURE, Optional.empty());
        builder.define(DATA_KAMIKOTIZATION, Optional.empty());
        builder.define(DATA_REPLICA_SOURCE, Optional.empty());
        builder.define(DATA_IS_REPLICA, false);
        builder.define(DATA_IS_RESTING, false);
    }

    public int getPowerLevel() {
        return entityData.get(DATA_POWER_LEVEL);
    }

    public void setPowerLevel(int level) {
        entityData.set(DATA_POWER_LEVEL, level);
    }

    public int getNameColor() {
        return entityData.get(DATA_NAME_COLOR);
    }

    public void setNameColor(int color) {
        entityData.set(DATA_NAME_COLOR, color);
    }

    public Optional<ResourceLocation> getFaceMaskTexture() {
        return entityData.get(DATA_FACE_MASK_TEXTURE);
    }

    public void setFaceMaskTexture(Optional<ResourceLocation> texture) {
        entityData.set(DATA_FACE_MASK_TEXTURE, texture);
    }

    public Optional<Holder<Kamikotization>> getKamikotization() {
        return entityData.get(DATA_KAMIKOTIZATION);
    }

    public void setKamikotization(Optional<Holder<Kamikotization>> kamikotization) {
        entityData.set(DATA_KAMIKOTIZATION, kamikotization);
    }

    public Optional<UUID> getReplicaSource() {
        return entityData.get(DATA_REPLICA_SOURCE);
    }

    public void setReplicaSource(Optional<UUID> source) {
        entityData.set(DATA_REPLICA_SOURCE, source);
    }

    public boolean isReplica() {
        return entityData.get(DATA_IS_REPLICA);
    }

    public void setReplica(boolean replica) {
        entityData.set(DATA_IS_REPLICA, replica);
    }

    public boolean isResting() {
        return entityData.get(DATA_IS_RESTING);
    }

    public void setResting(boolean resting) {
        entityData.set(DATA_IS_RESTING, resting);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SmoothFlyingPathNavigation(this, level);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
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
    protected void customServerAiStep() {
        if (getOwnerUUID() == null) {
            MineraculousConstants.LOGGER.warn("Kamiko {} has no owner, discarding...", getUUID());
            discard();
        }
        super.customServerAiStep();
        tickBrain(this);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !(source.is(MineraculousDamageTypeTags.HURTS_KAMIKOS) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isCreativePlayer());
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 1)
                .add(Attributes.GRAVITY, 0)
                .add(Attributes.FOLLOW_RANGE, 1024);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Kamiko>> getSensors() {
        return ObjectArrayList.of(
                new PlayerItemTemptingSensor<Kamiko>().temptedWith((kamiko, player, stack) -> {
                    UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
                    Entity caneOwner = ownerId != null ? player.level().getEntities().get(ownerId) : null;
                    if (caneOwner != null) {
                        MiraculousesData ownerMiraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                        MiraculousData storingData = ownerMiraculousesData.get(ownerMiraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE));
                        return stack.get(MineraculousDataComponents.BUTTERFLY_CANE_MODE) == ButterflyCaneItem.Mode.KAMIKO_STORE && storingData != null && storingData.storedEntities().isEmpty();
                    }
                    return false;
                }),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<Kamiko>().setPredicate((target, kamiko) -> target != kamiko && target.isAlive() && target instanceof Player));
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<Kamiko>().whenStopping(this::onMoveToWalkTargetStopping));
    }

    protected void onMoveToWalkTargetStopping(Kamiko kamiko) {
        if (BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.LOOKING_FOR_RESTING_LOCATION && hasReachedTarget(kamiko, BrainUtils.getMemory(kamiko, MemoryModuleType.WALK_TARGET))) {
            BrainUtils.setMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), ReplicationState.REPLICATING);
            BrainUtils.setMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get(), SharedConstants.TICKS_PER_SECOND);
        }
    }

    protected boolean hasReachedTarget(Kamiko entity, WalkTarget target) {
        return target != null && target.getTarget().currentBlockPosition().distManhattan(entity.blockPosition()) <= target.getCloseEnoughDist();
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new TargetOrRetaliate<Kamiko>() {
                            @Override
                            protected boolean checkExtraStartConditions(ServerLevel level, Kamiko entity) {
                                if (tickCount > SharedConstants.TICKS_PER_SECOND && !super.checkExtraStartConditions(level, entity)) {
                                    MineraculousConstants.LOGGER.info("Couldn't find target for Kamiko replica {}, discarding...", entity.getUUID());
                                    entity.discard();
                                }
                                return true;
                            }
                        }.startCondition(Kamiko::isReplica),
                        new FollowOwner<Kamiko>().startCondition(Kamiko::shouldFollowOwner),
                        new FollowTemptation<>(),
                        new SetRandomFlyingTarget<>()));
    }

    protected boolean shouldFollowOwner() {
        LivingEntity owner = getOwner();
        LivingEntity target = getTarget();
        return owner != null && (target == null || target == owner) && (owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent());
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<Kamiko>() {
                    @Override
                    protected boolean canAttack(Kamiko kamiko, LivingEntity target) {
                        UUID targetId = target.getUUID();
                        if (kamiko.isReplica() && (targetId.equals(kamiko.getOwnerUUID()) || getReplicaSource().map(source -> source.equals(targetId)).orElse(false)))
                            return false;
                        return kamiko.canBeSeenByAnyone();
                    }
                }.invalidateIf((EntityUtils.TARGET_TOO_FAR_PREDICATE::test)),
                new SetWalkTargetToAttackTarget<Kamiko>().startCondition(kamiko -> {
                    LivingEntity target = kamiko.getTarget();
                    if (target == null)
                        return false;
                    return checkTargetAndAlertTransformedOwner(target);
                }));
    }

    public boolean checkTargetAndAlertTransformedOwner(LivingEntity target) {
        boolean delay = target != null && target.getUUID().equals(getOwnerUUID()) && target.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed();
        if (delay && target instanceof Player player) {
            player.displayClientMessage(DETRANSFORM_TO_TRANSFORM, true);
        }
        return !delay;
    }

    public BrainActivityGroup<? extends Kamiko> getRestTasks() {
        return new BrainActivityGroup<Kamiko>(Activity.REST).priority(0).behaviours(
                new FirstApplicableBehaviour<>(
                        new Replicate<Kamiko>()
                                .onReplication(this::onReplication)
                                .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.REPLICATING)
                                .whenStarting(kamiko -> kamiko.setResting(true))
                                .whenStopping(kamiko -> {
                                    if (BrainUtils.memoryOrDefault(kamiko, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), () -> 0) >= MineraculousServerConfig.get().maxKamikoReplicas.getAsInt()) {
                                        kamiko.setResting(false);
                                    }
                                }),
                        new FirstApplicableBehaviour<>(
                                new SetRandomWalkTarget<>(),
                                new SetRandomHoverTarget<>())
                                        .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.LOOKING_FOR_RESTING_LOCATION)))
                .onlyStartWithMemoryStatus(MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected void onReplication(Kamiko replica, Kamiko original) {
        replica.setReplica(true);
        replica.setReplicaSource(original.getReplicaSource());
        replica.setKamikotization(original.getKamikotization());
        replica.setOwnerUUID(original.getOwnerUUID());
        AbilityReversionEntityData.get((ServerLevel) replica.level()).putCopied(original, replica);
    }

    @Override
    public Map<Activity, BrainActivityGroup<? extends Kamiko>> getAdditionalTasks() {
        return Util.make(new Reference2ReferenceOpenHashMap<>(), map -> {
            map.put(Activity.REST, getRestTasks());
        });
    }

    @Override
    public List<Activity> getActivityPriorities() {
        return ObjectArrayList.of(Activity.FIGHT, Activity.REST, Activity.IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> state.setAndContinue(DefaultAnimations.FLY)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    public void playerTouch(Player player) {
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner) {
            if (isReplica()) {
                getKamikotization().ifPresentOrElse(kamikotization -> {
                    KamikotizedMinion minion = new KamikotizedMinion((ServerPlayer) player, kamikotization);
                    getReplicaSource().ifPresent(minion::setOwnerUUID);
                    level().addFreshEntity(minion);
                    // TODO: Mark for reversion
                    discard();
                }, () -> {
                    MineraculousConstants.LOGGER.error("Kamiko replica {} has no kamikotization, discarding...", getUUID());
                    discard();
                });
            } else if (checkTargetAndAlertTransformedOwner(player)) {
                if (player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
                    owner.displayClientMessage(CANT_KAMIKOTIZE_TRANSFORMED, true);
                    setTarget(null);
                    return;
                }
                if (player != owner) {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(player), owner);
                    player.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).add(owner.getUUID());
                }
                owner.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(true).save(owner);
                discard();
                TommyLibServices.NETWORK.sendToClient(new ClientboundBeginKamikotizationSelectionPayload(player.getUUID(), new KamikoData(getUUID(), getOwnerUUID(), getPowerLevel(), getNameColor(), getFaceMaskTexture())), owner);
            }
        }
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        super.setOwnerUUID(uuid);
        if (uuid != null && level() instanceof ServerLevel level) {
            if (level.getEntity(uuid) instanceof LivingEntity owner) {
                MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
                if (!transformed.isEmpty()) {
                    Holder<Miraculous> first = transformed.getFirst();
                    setPowerLevel(miraculousesData.get(first).powerLevel() / 10);
                    Miraculous value = first.value();
                    setNameColor(value.color().getValue());
                    SpectateEntityAbility ability = (SpectateEntityAbility) Ability.getFirstMatching(a -> a instanceof SpectateEntityAbility sEA && sEA.isValidEntity(level, owner, this), value, true);
                    if (ability != null) {
                        setFaceMaskTexture(ability.faceMaskTexture());
                    }
                } else {
                    owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                        setPowerLevel(data.kamikoData().powerLevel() / 10);
                        setNameColor(data.kamikoData().nameColor());
                        setFaceMaskTexture(data.kamikoData().faceMaskTexture());
                    });
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        getKamikotization().ifPresent(kamikotization -> Kamikotization.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), kamikotization).getOrThrow());
        compound.putBoolean("IsResting", isResting());
        compound.putBoolean("IsReplica", isReplica());
        ReplicationState replicationState = BrainUtils.getMemory(this, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get());
        if (replicationState != null) {
            compound.putString("ReplicationStatus", replicationState.name());
        }
        Integer replicasMade = BrainUtils.getMemory(this, MineraculousMemoryModuleTypes.REPLICAS_MADE.get());
        if (replicasMade != null) {
            compound.putInt("ReplicasMade", replicasMade);
        }
        Integer replicationWaitTicks = BrainUtils.getMemory(this, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get());
        if (replicationWaitTicks != null) {
            compound.putInt("ReplicationWaitTicks", replicationWaitTicks);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setKamikotization(Kamikotization.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compound.get("Kamikotization")).result());
        setResting(compound.getBoolean("IsResting"));
        setReplica(compound.getBoolean("IsReplica"));
        if (compound.contains("ReplicationStatus")) {
            BrainUtils.setMemory(this, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), ReplicationState.valueOf(compound.getString("ReplicationStatus")));
        }
        if (compound.contains("ReplicasMade")) {
            BrainUtils.setMemory(this, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), compound.getInt("ReplicasMade"));
        }
        if (compound.contains("ReplicationWaitTicks")) {
            BrainUtils.setMemory(this, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get(), compound.getInt("ReplicationWaitTicks"));
        }
    }
}
