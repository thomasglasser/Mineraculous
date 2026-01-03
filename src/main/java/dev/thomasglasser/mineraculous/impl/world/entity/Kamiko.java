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
import dev.thomasglasser.mineraculous.api.world.entity.animal.ButterflyVariants;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundBeginKamikotizationSelectionPayload;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.animal.Butterfly;
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
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetAttackTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class Kamiko extends Butterfly implements TamableEntity {
    public static final ResourceLocation SPECTATOR_SHADER = MineraculousConstants.modLoc("shaders/post/kamiko.json");
    public static final Component DETRANSFORM_TO_TRANSFORM = Component.translatable("entity.mineraculous.kamiko.detransform_to_transform");
    public static final Component CANT_KAMIKOTIZE_TRANSFORMED = Component.translatable("entity.mineraculous.kamiko.cant_kamikotize_transformed");

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_POWER_LEVEL = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_NAME_COLOR = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<ResourceLocation>> DATA_FACE_MASK_TEXTURE = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_RESOURCE_LOCATION.get());
    private static final EntityDataAccessor<Optional<UUID>> DATA_REPLICA_SOURCE = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<Holder<Kamikotization>>> DATA_KAMIKOTIZATION = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_KAMIKOTIZATION.get());
    private static final EntityDataAccessor<String> DATA_REPLICA_NAME = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_REPLICA_TOOL_COUNT = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_REPLICA = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private LivingEntity owner = null;

    public Kamiko(EntityType<? extends Kamiko> type, Level level) {
        super(type, level);
        setVariant(registryAccess().holderOrThrow(ButterflyVariants.KAMIKO));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_POWER_LEVEL, 0);
        builder.define(DATA_NAME_COLOR, -1);
        builder.define(DATA_FACE_MASK_TEXTURE, Optional.empty());
        builder.define(DATA_REPLICA_SOURCE, Optional.empty());
        builder.define(DATA_KAMIKOTIZATION, Optional.empty());
        builder.define(DATA_REPLICA_NAME, "");
        builder.define(DATA_REPLICA_TOOL_COUNT, 0);
        builder.define(DATA_IS_REPLICA, false);
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

    public Optional<UUID> getReplicaSource() {
        return entityData.get(DATA_REPLICA_SOURCE);
    }

    public void setReplicaSource(Optional<UUID> source) {
        entityData.set(DATA_REPLICA_SOURCE, source);
    }

    public Optional<Holder<Kamikotization>> getKamikotization() {
        return entityData.get(DATA_KAMIKOTIZATION);
    }

    public void setKamikotization(Optional<Holder<Kamikotization>> kamikotization) {
        entityData.set(DATA_KAMIKOTIZATION, kamikotization);
    }

    public String getReplicaName() {
        return entityData.get(DATA_REPLICA_NAME);
    }

    public void setReplicaName(String name) {
        entityData.set(DATA_REPLICA_NAME, name);
    }

    public int getReplicaToolCount() {
        return entityData.get(DATA_REPLICA_TOOL_COUNT);
    }

    public void setReplicaToolCount(int toolCount) {
        entityData.set(DATA_REPLICA_TOOL_COUNT, toolCount);
    }

    public boolean isReplica() {
        return entityData.get(DATA_IS_REPLICA);
    }

    public void setReplica(boolean replica) {
        entityData.set(DATA_IS_REPLICA, replica);
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        if (this.owner != null && (this.owner.isRemoved() || !this.owner.getUUID().equals(getOwnerUUID())))
            this.owner = null;

        if (this.owner == null)
            this.owner = TamableEntity.super.getOwner();

        return this.owner;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER, Optional.ofNullable(uuid));
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
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData existing = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        return existing;
    }

    @Override
    protected void customServerAiStep() {
        if (getOwnerUUID() == null) {
            MineraculousConstants.LOGGER.warn("Kamiko {} has no owner, discarding...", getUUID());
            discard();
        }
        super.customServerAiStep();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !(source.is(MineraculousDamageTypeTags.HURTS_KAMIKOS) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isCreativePlayer());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Butterfly.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 1024);
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
    protected void onMoveToWalkTargetStopping(Butterfly butterfly) {
        if (BrainUtils.getMemory(butterfly, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.LOOKING_FOR_RESTING_LOCATION && hasReachedTarget(butterfly, BrainUtils.getMemory(butterfly, MemoryModuleType.WALK_TARGET))) {
            BrainUtils.setMemory(butterfly, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), ReplicationState.REPLICATING);
            BrainUtils.setMemory(butterfly, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get(), SharedConstants.TICKS_PER_SECOND);
        }
    }

    @Override
    public BrainActivityGroup<? extends Butterfly> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new SetAttackTarget<Kamiko>(false).targetFinder(Kamiko::findNearestForceKamikotizeTarget).startCondition(Kamiko::isReplica),
                        new FollowEntity<Kamiko, LivingEntity>().following(Kamiko::getOwner).startCondition(Kamiko::shouldFollowOwner),
                        new FollowTemptation<>(),
                        new SetRandomFlyingTarget<>()));
    }

    protected boolean shouldFollowOwner() {
        LivingEntity owner = getOwner();
        LivingEntity target = getTarget();
        return owner != null && (target == null || target == owner) && (owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent());
    }

    @Override
    public BrainActivityGroup<? extends Butterfly> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<Kamiko>() {
                    @Override
                    protected boolean canAttack(Kamiko kamiko, LivingEntity target) {
                        return kamiko.canBeSeenByAnyone() && (!kamiko.isReplica() || kamiko.canForceKamikotize(target));
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

    public boolean canForceKamikotize(LivingEntity target) {
        UUID targetId = target.getUUID();
        if (targetId.equals(getOwnerUUID()) || getReplicaSource().map(targetId::equals).orElse(false)) return false;
        if (target.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).spectatingId().isPresent()) return false;
        // TODO: Convert to event
        if (target instanceof Player player && player.getAbilities().instabuild && !MineraculousServerConfig.get().forceKamikotizeCreativePlayers.getAsBoolean())
            return false;
        return target.isAlive();
    }

    protected @Nullable LivingEntity findNearestForceKamikotizeTarget() {
        List<LivingEntity> nearbyEntities = BrainUtils.getMemory(this, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (nearbyEntities != null) {
            for (LivingEntity target : nearbyEntities) {
                if (canForceKamikotize(target)) {
                    return target;
                }
            }
        }
        MineraculousConstants.LOGGER.info("Couldn't find target for Kamiko replica {}, discarding...", getUUID());
        discard();
        return null;
    }

    public BrainActivityGroup<? extends Butterfly> getRestTasks() {
        return new BrainActivityGroup<Butterfly>(Activity.REST).priority(0).behaviours(
                new FirstApplicableBehaviour<>(
                        new Replicate<Butterfly>()
                                .maxReplicas(MineraculousServerConfig.get().maxKamikoReplicas)
                                .onReplication((replica, original) -> onReplication((Kamiko) replica, (Kamiko) original))
                                .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.REPLICATING)
                                .whenStarting(kamiko -> kamiko.setResting(true))
                                .whenStopping(kamiko -> {
                                    if (BrainUtils.memoryOrDefault(kamiko, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), () -> 0) >= MineraculousServerConfig.get().maxKamikoReplicas.getAsInt()) {
                                        kamiko.setResting(false);
                                    }
                                }),
                        new SetRandomWalkTarget<>()
                                .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get()) == ReplicationState.LOOKING_FOR_RESTING_LOCATION)))
                .onlyStartWithMemoryStatus(MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected void onReplication(Kamiko replica, Kamiko original) {
        List<SynchedEntityData.DataValue<?>> values = original.entityData.getNonDefaultValues();
        if (values != null) {
            replica.entityData.assignValues(values);
        }
        replica.setReplica(true);
        EntityReversionData.get((ServerLevel) replica.level()).putCopied(original, replica);
    }

    @Override
    public Map<Activity, BrainActivityGroup<? extends Butterfly>> getAdditionalTasks() {
        return Util.make(new Reference2ReferenceOpenHashMap<>(), map -> map.put(Activity.REST, getRestTasks()));
    }

    @Override
    public List<Activity> getActivityPriorities() {
        return ObjectArrayList.of(Activity.FIGHT, Activity.REST, Activity.IDLE);
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
            if (isReplica()) {
                getKamikotization().ifPresentOrElse(kamikotization -> {
                    KamikotizedMinion minion = new KamikotizedMinion((ServerPlayer) player, kamikotization, new KamikoData(this), getReplicaName(), getReplicaToolCount());
                    minion.setOwnerUUID(getReplicaSource().orElseThrow(() -> new IllegalStateException("Tried to summon Kamikotized Minion without replica source")));
                    level().addFreshEntity(minion);
                    EntityReversionData.get((ServerLevel) level()).putRemovable(getOwnerUUID(), minion);
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
                TommyLibServices.NETWORK.sendToClient(new ClientboundBeginKamikotizationSelectionPayload(player.getUUID(), new KamikoData(this)), owner);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null)
            compound.putUUID("Owner", ownerUUID);
        getReplicaSource().ifPresent(uuid -> compound.putUUID("ReplicaSource", uuid));
        RegistryOps<Tag> ops = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        getKamikotization().ifPresent(kamikotization -> compound.put("Kamikotization", Kamikotization.CODEC.encodeStart(ops, kamikotization).getOrThrow()));
        compound.putString("ReplicaName", getReplicaName());
        compound.putInt("ReplicaToolCount", getReplicaToolCount());
        compound.putBoolean("IsReplica", isReplica());
        compound.putBoolean("IsResting", isResting());
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
        setOwnerUUID(compound.contains("Owner") ? compound.getUUID("Owner") : null);
        setReplicaSource(compound.contains("ReplicaSource") ? Optional.of(compound.getUUID("ReplicaSource")) : Optional.empty());
        RegistryOps<Tag> ops = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        setKamikotization(Kamikotization.CODEC.parse(ops, compound.get("Kamikotization")).result());
        setReplicaName(compound.getString("ReplicaName"));
        setReplicaToolCount(compound.getInt("ReplicaToolCount"));
        setReplica(compound.getBoolean("IsReplica"));
        setResting(compound.getBoolean("IsResting"));
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
