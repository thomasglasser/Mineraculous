package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.SpectateEntityAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.ai.sensing.PlayerItemTemptingSensor;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundBeginKamikotizationSelectionPayload;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
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
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kamiko extends TamableAnimal implements SmartBrainOwner<Kamiko>, GeoEntity {
    public static final ResourceLocation SPECTATOR_SHADER = MineraculousConstants.modLoc("shaders/post/kamiko.json");
    public static final Component DETRANSFORM_TO_TRANSFORM = Component.translatable("entity.mineraculous.kamiko.detransform_to_transform");
    public static final Component CANT_KAMIKOTIZE_TRANSFORMED = Component.translatable("entity.mineraculous.kamiko.cant_kamikotize_transformed");

    private static final EntityDataAccessor<Integer> DATA_POWER_LEVEL = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_NAME_COLOR = SynchedEntityData.defineId(Kamiko.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<ResourceLocation>> DATA_FACE_MASK_TEXTURE = SynchedEntityData.defineId(Kamiko.class, MineraculousEntityDataSerializers.OPTIONAL_RESOURCE_LOCATION.get());

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
                new SetWalkTargetToAttackTarget<Kamiko>().startCondition(kamiko -> {
                    LivingEntity target = kamiko.getTarget();
                    if (target == null)
                        return false;
                    return checkTargetAndAlertTransformedOwner(target);
                }),
                new MoveToWalkTarget<Kamiko>());
    }

    public boolean checkTargetAndAlertTransformedOwner(LivingEntity target) {
        boolean delay = target != null && target.getUUID().equals(getOwnerUUID()) && target.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed();
        if (delay && target instanceof Player player) {
            player.displayClientMessage(DETRANSFORM_TO_TRANSFORM, true);
        }
        return !delay;
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (!this.isOrderedToSit()) {
                return state.setAndContinue(DefaultAnimations.FLY);
            }
            return PlayState.STOP;
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
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner && checkTargetAndAlertTransformedOwner(player)) {
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
}
