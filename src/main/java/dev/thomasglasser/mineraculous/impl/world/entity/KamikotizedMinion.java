package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityDataSerializers;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.AbilityEffectUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class KamikotizedMinion extends PathfinderMob implements SmartBrainOwner<KamikotizedMinion>, PlayerLike, OwnableEntity {
    private static final EntityDataAccessor<UUID> DATA_SOURCE_ID = SynchedEntityData.defineId(KamikotizedMinion.class, MineraculousEntityDataSerializers.UUID.get());
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(KamikotizedMinion.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<Holder<Kamikotization>>> DATA_KAMIKOTIZATION = SynchedEntityData.defineId(KamikotizedMinion.class, MineraculousEntityDataSerializers.OPTIONAL_KAMIKOTIZATION.get());

    protected double xCloakO, yCloakO, zCloakO;
    protected double xCloak, yCloak, zCloak;
    protected float oBob, bob;

    protected GameType previousGameMode = GameType.DEFAULT_MODE;

    @Nullable
    private Player storedSource = null;
    @Nullable
    private Player storedOwner = null;

    public KamikotizedMinion(EntityType<? extends KamikotizedMinion> entityType, Level level) {
        super(entityType, level);
    }

    public KamikotizedMinion(ServerPlayer source, Holder<Kamikotization> kamikotization) {
        this(MineraculousEntityTypes.KAMIKOTIZED_MINION.get(), source.level());
        setSourceId(source.getUUID());
        setKamikotization(kamikotization);
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot), kamikotization);
            stack.enchant(level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS, Unit.INSTANCE);
            setItemSlot(slot, stack);
        }
        moveTo(source.position(), source.getYRot(), source.getXRot());
        setYBodyRot(source.yBodyRot);
        setYHeadRot(source.yHeadRot);
        this.getAttributes().assignAllValues(source.getAttributes());
        PlayerLike.adjustPlayerAttributes(this);
        previousGameMode = source.gameMode.getGameModeForPlayer();
        source.setGameMode(GameType.SPECTATOR);
        AbilityEffectUtils.beginSpectation(source, Optional.of(getUUID()), Optional.empty(), Optional.empty(), Optional.empty(), false, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SOURCE_ID, Util.NIL_UUID);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_KAMIKOTIZATION, Optional.empty());
    }

    public UUID getSourceId() {
        return this.getEntityData().get(DATA_SOURCE_ID);
    }

    public void setSourceId(UUID id) {
        this.getEntityData().set(DATA_SOURCE_ID, id);
    }

    public @Nullable Player getSource() {
        UUID source = getSourceId();
        if (storedSource == null || !storedSource.getUUID().equals(source)) {
            storedSource = source != null ? level().getPlayerByUUID(source) : null;
        }
        return storedSource;
    }

    public Optional<Player> getOptionalSource() {
        return Optional.ofNullable(getSource());
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.getEntityData().get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.getEntityData().set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    public @Nullable Player getOwner() {
        UUID owner = getOwnerUUID();
        if (storedOwner == null || !storedOwner.getUUID().equals(owner)) {
            storedOwner = owner != null ? level().getPlayerByUUID(owner) : null;
        }
        return storedOwner;
    }

    public Optional<Holder<Kamikotization>> getKamikotization() {
        return this.getEntityData().get(DATA_KAMIKOTIZATION);
    }

    public void setKamikotization(Holder<Kamikotization> kamikotization) {
        this.getEntityData().set(DATA_KAMIKOTIZATION, Optional.of(kamikotization));
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
    public void aiStep() {
        this.oBob = this.bob;
        super.aiStep();
        Player owner = getOwner();
        setNoAi(owner == null || !owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization).equals(getKamikotization()));
        float f;
        if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
            f = Math.min(0.1F, (float) this.getDeltaMovement().horizontalDistance());
        } else {
            f = 0.0F;
        }
        this.bob = this.bob + (f - this.bob) * 0.4F;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            ServerPlayer source = (ServerPlayer) getSource();
            if (tickCount > SharedConstants.TICKS_PER_SECOND && (source == null || !source.isAlive() || source.isRemoved())) {
                MineraculousConstants.LOGGER.warn("KamikotizedMinion {} has no source, discarding...", getUUID());
                discard();
                return;
            } else if (source != null && source.tickCount > SharedConstants.TICKS_PER_SECOND && source.getCamera() != this) {
                source.setCamera(this);
            }
        }
        super.tick();
        moveCloak();
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d0 = this.getX() - this.xCloak;
        double d1 = this.getY() - this.yCloak;
        double d2 = this.getZ() - this.zCloak;
        double d3 = 10.0;
        if (d0 > d3) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 > d3) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 > d3) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        if (d0 < -d3) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 < -d3) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 < -d3) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        this.xCloak += d0 * 0.25;
        this.zCloak += d2 * 0.25;
        this.yCloak += d1 * 0.25;
    }

    @Override
    public List<? extends ExtendedSensor<? extends KamikotizedMinion>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<KamikotizedMinion>().setPredicate((target, entity) -> {
                    if (target.isAlliedTo(entity)) return false;
                    Player owner = entity.getOwner();
                    if (owner != null) {
                        if (target.isAlliedTo(owner)) return false;
                        if (target instanceof OwnableEntity ownableEntity && owner.getUUID().equals(ownableEntity.getOwnerUUID()))
                            return false;
                        if (target.getLastHurtByMob() != null && target.getLastHurtByMob().is(owner))
                            return true;
                        if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().is(target))
                            return true;
                        if (BrainUtils.hasMemory(target.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
                            return BrainUtils.getTargetOfEntity(target) == owner;
                        } else if (target instanceof Mob mob) {
                            return mob.getTarget() == owner;
                        }
                    }
                    return false;
                }),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends KamikotizedMinion> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new FloatToSurfaceOfFluid<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends KamikotizedMinion> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new SetPlayerLookTarget<>().lookPredicate((minion, player) -> player == getTarget() || player == getOptionalSource().orElseThrow()),
                new FirstApplicableBehaviour<>(
                        new TargetOrRetaliate<>(),
                        new FollowEntity<KamikotizedMinion, Player>().following(KamikotizedMinion::getOwner).teleportToTargetAfter(12)));
    }

    @Override
    public BrainActivityGroup<? extends KamikotizedMinion> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<>(),
                // TODO: Support ranged and powers
                new AnimatableMeleeAttack<>(0));
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        unbindSource();
    }

    public void unbindSource() {
        if (!level().isClientSide()) {
            ServerPlayer source = (ServerPlayer) getSource();
            if (source != null) {
                AbilityEffectUtils.endSpectation(source);
                source.setGameMode(previousGameMode);
            }
            setSourceId(Util.NIL_UUID);
        }
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return BrainUtils.getTargetOfEntity(this);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, target);
    }

    @Override
    public Component getDisplayName() {
        if (!hasCustomName()) {
            return this.getOptionalSource().orElseThrow().getDisplayName();
        }
        return super.getDisplayName();
    }

    @Override
    public @Nullable CompoundTag getShoulderEntityLeft() {
        return getOptionalSource().orElseThrow().getShoulderEntityLeft();
    }

    @Override
    public @Nullable CompoundTag getShoulderEntityRight() {
        return getOptionalSource().orElseThrow().getShoulderEntityRight();
    }

    // TODO: Fix, cache on server...?
    @Override
    public PlayerSkin getSkin() {
        if (getOwner() instanceof AbstractClientPlayer player)
            return player.getSkin();
        else if (getSource() instanceof AbstractClientPlayer player)
            return player.getSkin();
        return new PlayerSkin(DefaultPlayerSkin.getDefaultTexture(), null, null, null, PlayerSkin.Model.SLIM, true);
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return getOptionalSource().orElseThrow().isModelPartShown(part);
    }

    @Override
    public Scoreboard getScoreboard() {
        return getOptionalSource().orElseThrow().getScoreboard();
    }

    @Override
    public double xCloakO() {
        return xCloakO;
    }

    @Override
    public double yCloakO() {
        return yCloakO;
    }

    @Override
    public double zCloakO() {
        return zCloakO;
    }

    @Override
    public double xCloak() {
        return xCloak;
    }

    @Override
    public double yCloak() {
        return yCloak;
    }

    @Override
    public double zCloak() {
        return zCloak;
    }

    @Override
    public float oBob() {
        return oBob;
    }

    @Override
    public float bob() {
        return bob;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putUUID("Source", getSourceId());
        UUID uuid = getOwnerUUID();
        if (uuid != null) {
            compound.putUUID("Owner", uuid);
        }
        compound.putString("PreviousGameMode", previousGameMode.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setSourceId(compound.getUUID("Source"));
        UUID uuid;
        if (compound.hasUUID("Owner")) {
            uuid = compound.getUUID("Owner");
        } else {
            String s = compound.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }
        setOwnerUUID(uuid);
        previousGameMode = GameType.byName(compound.getString("PreviousGameMode"));
    }
}
