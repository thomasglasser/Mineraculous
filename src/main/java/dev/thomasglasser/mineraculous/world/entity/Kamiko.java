package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundOpenKamikotizationSelectionScreenPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.TemptingAbility;
import dev.thomasglasser.mineraculous.world.entity.ai.sensing.PlayerItemTemptingSensor;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import software.bernie.geckolib.animation.PlayState;
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
        return !(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(MineraculousDamageTypes.CATACLYSM));
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
                new PlayerItemTemptingSensor<Kamiko>().temptedWith((entity, player, stack) -> {
                    if (shouldFollowOwner(entity)) {
                        return true;
                    }
                    ResolvableProfile profile = stack.get(DataComponents.PROFILE);
                    Player caneOwner = profile != null ? player.level().getPlayerByUUID(profile.id().orElse(profile.gameProfile().getId())) : null;
                    if (caneOwner != null) {
                        MiraculousesData ownerMiraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                        MiraculousData storingData = ownerMiraculousesData.get(ownerMiraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE));
                        return stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && storingData.storedEntities().isEmpty();
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
                new MoveToWalkTarget<Kamiko>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrainActivityGroup<? extends Kamiko> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new FollowOwner<Kamiko>().startCondition(this::shouldFollowOwner),
                        new FollowTemptation<>(),
                        new SetRandomFlyingTarget<>()));
    }

    protected boolean shouldFollowOwner(Kamiko kamiko) {
        if (BrainUtils.hasMemory(kamiko.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        LivingEntity owner = kamiko.getOwner();
        if (owner != null && level() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (Holder<Miraculous> key : miraculousesData.getTransformed()) {
                MiraculousData data = miraculousesData.get(key);
                if (Ability.hasMatching(ability -> ability instanceof TemptingAbility temptingAbility && temptingAbility.shouldTempt(level, owner.position(), kamiko), key.value(), data.powerActive())) {
                    return true;
                }
            }
            if (owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                KamikotizationData kamikotizationData = owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                return Ability.hasMatching(ability -> ability instanceof TemptingAbility temptingAbility && temptingAbility.shouldTempt(level, owner.position(), kamiko), kamikotizationData.kamikotization().value(), kamikotizationData.powerActive());
            }
        }
        return false;
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
        if (getTarget() == player && getOwner() instanceof ServerPlayer owner) {
            if (player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
                owner.displayClientMessage(Component.translatable(CANT_KAMIKOTIZE_TRANSFORMED), true);
                setTarget(null);
                return;
            }
//            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncKamikotizationLooksPayload(owner.getUUID(), Kamikotization.getFor(player)), (ServerPlayer) player);
            TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(player), owner);
            player.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).add(owner.getUUID());
            owner.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withSpectationInterrupted().save(owner, true);
            remove(RemovalReason.DISCARDED);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenKamikotizationSelectionScreenPayload(player.getUUID(), new KamikoData(getUUID(), getOwnerUUID(), getNameColor(), getFaceMaskTexture())), owner);
        }
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        super.setOwnerUUID(uuid);
        if (uuid != null && level() instanceof ServerLevel level) {
            if (level.getEntity(uuid) instanceof Entity owner) {
                List<Holder<Miraculous>> transformed = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed();
                if (!transformed.isEmpty()) {
                    setNameColor(transformed.getFirst().value().color().getValue());
                } else {
                    owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                        setNameColor(data.kamikoData().nameColor());
                    });
                }
            }
        }
    }
}
