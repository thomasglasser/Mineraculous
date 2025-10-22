package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.SpectateEntityAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.ai.behavior.Duplicate;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.DuplicationState;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.api.world.entity.ai.sensing.PlayerItemTemptingSensor;
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
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomHoverTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.navigation.SmoothFlyingPathNavigation;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
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
    public static final Component CANT_KAMIKOTIZE_TRANSFORMED = Component.translatable("entity.mineraculous.kamiko.cant_kamikotize_transformed");

    private static final EntityDataAccessor<Integer> DATA_POWER_LEVEL = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_NAME_COLOR = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<ResourceLocation>> DATA_FACE_MASK_TEXTURE = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_RESOURCE_LOCATION.get());
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
        return !source.is(MineraculousDamageTypeTags.HURTS_KAMIKOS);
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
                }));
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new InvalidateAttackTarget<>() {
                    @Override
                    protected boolean canAttack(LivingEntity entity, LivingEntity target) {
                        return entity.canBeSeenByAnyone();
                    }
                }.invalidateIf(EntityUtils.TARGET_TOO_FAR_PREDICATE),
                new SetWalkTargetToAttackTarget<Kamiko>(),
                new MoveToWalkTarget<Kamiko>().whenStopping(this::onMoveToWalkTargetStopping));
    }

    protected void onMoveToWalkTargetStopping(Kamiko kamiko) {
        if (BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get()) == DuplicationState.LOOKING_FOR_RESTING_LOCATION && hasReachedTarget(kamiko, BrainUtils.getMemory(kamiko, MemoryModuleType.WALK_TARGET))) {
            BrainUtils.setMemory(kamiko, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), DuplicationState.DUPLICATING);
        }
    }

    protected boolean hasReachedTarget(Kamiko entity, WalkTarget target) {
        return target != null && target.getTarget().currentBlockPosition().distManhattan(entity.blockPosition()) <= target.getCloseEnoughDist();
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new FollowOwner<Kamiko>().startCondition(this::shouldFollowOwner),
                        new FollowTemptation<>(),
                        new SetRandomFlyingTarget<>()));
    }

    protected boolean shouldFollowOwner(Kamiko kamiko) {
        LivingEntity owner = getOwner();
        return owner != null && !BrainUtils.hasMemory(kamiko.getBrain(), MemoryModuleType.ATTACK_TARGET) && (owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent());
    }

    public BrainActivityGroup<? extends Kamiko> getRestTasks() {
        return new BrainActivityGroup<Kamiko>(Activity.REST).priority(0).behaviours(
                new FirstApplicableBehaviour<>(
                        new Duplicate<Kamiko>()
                                .onDuplication(this::onDuplication)
                                .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get()) == DuplicationState.DUPLICATING)
                                .whenStarting(kamiko -> kamiko.setResting(true))
                                .whenStopping(kamiko -> {
                                    if (BrainUtils.memoryOrDefault(kamiko, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0) >= MineraculousServerConfig.get().maxKamikoDuplicates.getAsInt()) {
                                        kamiko.setResting(false);
                                    }
                                }),
                        new FirstApplicableBehaviour<>(
                                new SetRandomWalkTarget<>(),
                                new SetRandomHoverTarget<>())
                                        .startCondition(kamiko -> BrainUtils.getMemory(kamiko, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get()) == DuplicationState.LOOKING_FOR_RESTING_LOCATION)))
                .onlyStartWithMemoryStatus(MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), MemoryStatus.VALUE_PRESENT);
    }

    protected void onDuplication(Kamiko duplicate, Kamiko original) {
        duplicate.setDeltaMovement(0, 0.5, 0);
        duplicate.setOwnerUUID(original.getOwnerUUID());
        AbilityReversionEntityData.get((ServerLevel) duplicate.level()).putCopied(original, duplicate);
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
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isResting()) {
                return state.setAndContinue(DefaultAnimations.IDLE);
            }
            return state.setAndContinue(DefaultAnimations.FLY);
        }));
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
    public void playerTouch(Player player) {
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner) {
            if (player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
                owner.displayClientMessage(CANT_KAMIKOTIZE_TRANSFORMED, true);
                setTarget(null);
                return;
            }
            TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(player), owner);
            player.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).add(owner.getUUID());
            owner.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withSpectationInterrupted().save(owner, true);
            remove(RemovalReason.DISCARDED);
            TommyLibServices.NETWORK.sendToClient(new ClientboundBeginKamikotizationSelectionPayload(player.getUUID(), new KamikoData(getUUID(), getOwnerUUID(), getPowerLevel(), getNameColor(), getFaceMaskTexture())), owner);
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
        DuplicationState duplicationState = BrainUtils.getMemory(this, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get());
        if (duplicationState != null) {
            compound.putString("DuplicationStatus", duplicationState.name());
        }
        Integer duplicatesMade = BrainUtils.getMemory(this, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get());
        if (duplicatesMade != null) {
            compound.putInt("DuplicatesMade", duplicatesMade);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("DuplicationStatus")) {
            BrainUtils.setMemory(this, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), DuplicationState.valueOf(compound.getString("DuplicationStatus")));
        }
        if (compound.contains("DuplicatesMade")) {
            BrainUtils.setMemory(this, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), compound.getInt("DuplicatesMade"));
        }
    }
}
