package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundOpenKamikotizationSelectionScreenPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncKamikotizationLooksPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.SetCameraEntityAbility;
import dev.thomasglasser.mineraculous.world.entity.ability.SetOwnerAbility;
import dev.thomasglasser.mineraculous.world.entity.ai.sensing.PlayerTemptingSensor;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.component.ResolvableProfile;
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
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Kamiko extends TamableAnimal implements SmartBrainOwner<Kamiko>, GeoEntity {
    public static final ResourceLocation SPECTATOR_SHADER = Mineraculous.modLoc("post_effect/kamiko.json");
    public static final String CANT_KAMIKOTIZE_TRANSFORMED = "entity.mineraculous.kamiko.cant_kamikotize_transformed";

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
        builder.define(DATA_NAME_COLOR, -1);
        builder.define(DATA_FACE_MASK_TEXTURE, Optional.empty());
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
    protected PathNavigation createNavigation(Level world) {
        return new SmoothFlyingPathNavigation(this, world);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public boolean isPowered() {
        return getOwnerUUID() != null;
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
        if (isPowered())
            return !(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(MineraculousDamageTypes.CATACLYSM));
        return super.isInvulnerableTo(source);
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
                new PlayerTemptingSensor<Kamiko>().temptedWith((entity, player, stack) -> {
                    MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    List<ResourceKey<?>> tempting = new ArrayList<>(miraculousesData.getTransformed().stream().filter(key -> {
                        Miraculous miraculous = level().holderOrThrow(key).value();
                        MiraculousData data = miraculousesData.get(key);
                        return Ability.hasMatching(ability -> ability instanceof SetOwnerAbility setOwnerAbility && setOwnerAbility.isValid(entity), miraculous, data.mainPowerActive());
                    }).toList());
                    if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                        KamikotizationData kamikotizationData = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                        if (Ability.hasMatching(ability -> ability instanceof SetOwnerAbility setOwnerAbility && setOwnerAbility.isValid(entity), player.level().holderOrThrow(kamikotizationData.kamikotization()).value(), kamikotizationData.mainPowerActive()))
                            tempting.add(kamikotizationData.kamikotization());
                    }
                    if (!tempting.isEmpty())
                        return true;
                    ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
                    Player caneOwner = resolvableProfile != null ? player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId())) : null;
                    if (caneOwner == null)
                        return false;
                    MiraculousData storingData = miraculousesData.get(miraculousesData.getFirstKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, level()));
                    return (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(ButterflyCaneItem.TAG_STORED_KAMIKO));
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
                new MoveToWalkTarget<Kamiko>());
    }

    @Override
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new FollowOwner<Kamiko>().startCondition(kamiko -> !BrainUtils.hasMemory(kamiko.getBrain(), MemoryModuleType.ATTACK_TARGET) && kamiko.getOwner() != null && kamiko.getOwner().getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()),
                        new FollowTemptation<>(),
                        new SetRandomFlyingTarget<>()));
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
    public void playerTouch(Player player) {
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner) {
            if (player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
                owner.displayClientMessage(Component.translatable(CANT_KAMIKOTIZE_TRANSFORMED), true);
                setTarget(null);
                return;
            }
            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncKamikotizationLooksPayload(owner.getUUID(), Kamikotization.getFor(player).stream().map(Holder::getKey).toList()), (ServerPlayer) player);
            TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(player), owner);
            owner.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withFaceMaskTexture(getFaceMaskTexture()).save(owner, true);
            remove(RemovalReason.DISCARDED);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenKamikotizationSelectionScreenPayload(player.getUUID(), new KamikoData(getUUID(), getOwnerUUID(), getNameColor(), getFaceMaskTexture())), owner);
        }
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        super.setOwnerUUID(uuid);
        if (uuid != null && level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(uuid) instanceof LivingEntity owner) {
                MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                boolean colorSet = false;
                boolean faceMaskTextureSet = false;
                for (ResourceKey<Miraculous> key : miraculousesData.getTransformed()) {
                    Miraculous miraculous = level().holderOrThrow(key).value();
                    if (!colorSet && Ability.hasMatching(a -> a instanceof SetOwnerAbility sOA && sOA.isValid(this), miraculous, true)) {
                        setNameColor(miraculous.color().getValue());
                        colorSet = true;
                    }
                    if (!faceMaskTextureSet) {
                        SetCameraEntityAbility ability = Ability.getFirstMatching(a -> a instanceof SetCameraEntityAbility sCEA && sCEA.faceMaskTexture().isPresent(), miraculous, true) instanceof SetCameraEntityAbility sCEA ? sCEA : null;
                        if (ability != null) {
                            setFaceMaskTexture(ability.faceMaskTexture());
                            faceMaskTextureSet = true;
                        }
                    }
                }
                if (owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    KamikotizationData kamikotizationData = owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                    if (Ability.hasMatching(ability -> ability instanceof SetOwnerAbility setOwnerAbility && setOwnerAbility.isValid(this), owner.level().holderOrThrow(kamikotizationData.kamikotization()).value(), true))
                        setNameColor(kamikotizationData.kamikoData().nameColor());
                    setFaceMaskTexture(kamikotizationData.kamikoData().faceMaskTexture());
                }
            }
        }
    }
}
