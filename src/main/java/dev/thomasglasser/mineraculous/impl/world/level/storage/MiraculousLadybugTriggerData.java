package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.EffectRevertingItem;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugBlockClusterTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugEntityTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public record MiraculousLadybugTriggerData(UUID performerId, UUID targetId, Optional<Holder<SoundEvent>> revertSound, int tickCount) {

    public static final Codec<MiraculousLadybugTriggerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("performer_id").forGetter(MiraculousLadybugTriggerData::performerId),
            UUIDUtil.CODEC.fieldOf("target_id").forGetter(MiraculousLadybugTriggerData::targetId),
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(MiraculousLadybugTriggerData::revertSound),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugTriggerData::tickCount)).apply(instance, MiraculousLadybugTriggerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTriggerData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MiraculousLadybugTriggerData::performerId,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugTriggerData::targetId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), MiraculousLadybugTriggerData::revertSound,
            ByteBufCodecs.INT, MiraculousLadybugTriggerData::tickCount,
            MiraculousLadybugTriggerData::new);

    public static final int MIRACULOUS_LADYBUGS_COUNT = 8;

    public MiraculousLadybugTriggerData(UUID performerId, UUID targetId, Optional<Holder<SoundEvent>> revertSound) {
        this(performerId, targetId, revertSound, 0);
    }

    public MiraculousLadybugTriggerData incrementTicks() {
        return new MiraculousLadybugTriggerData(performerId, targetId, revertSound, tickCount + 1);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER, Optional.of(this));
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER);
    }

    @Nullable
    public LivingEntity getPerformer(ServerLevel level) {
        return level.getEntity(performerId) instanceof LivingEntity performer ? performer : null;
    }

    public void tick(Entity entity, ServerLevel level) {
        double y = entity.getDeltaMovement().y;
        if (entity.isNoGravity() || (!entity.isNoGravity() && y < 0.13)) {
            LivingEntity performer = this.getPerformer(level);
            this.incrementTicks().save(entity);
            entity.setNoGravity(true);
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
            if (tickCount == 1) {
                if (performer != null)
                    Ability.playSound(level, performer, revertSound);
                spawnSphereParticles(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), level, entity.position(), 200);
                spawnSphereParticles(ParticleTypes.END_ROD, level, entity.position(), 10);
            } else if (tickCount > 20) {
                spawnMiraculousLadybugs(level, entity);
                if (performer != null)
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performer.getUUID());
                entity.discard();
            }
        }
    }

    private static void spawnSphereParticles(SimpleParticleType type, ServerLevel level, Vec3 vec3, int particleCount) {
        double radius = 0.5;

        for (int i = 0; i < particleCount; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            double r = radius * Math.cbrt(Math.random());

            double xOffset = r * Math.sin(phi) * Math.cos(theta);
            double yOffset = r * Math.cos(phi);
            double zOffset = r * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(
                    type,
                    vec3.x + xOffset,
                    vec3.y + yOffset,
                    vec3.z + zOffset,
                    1,
                    0, 0, 0, 0);
        }
    }

    private void spawnMiraculousLadybugs(ServerLevel level, Entity entity) {
        ArrayList<Vector2d> circle = MineraculousMathUtils.generateCirclePoints(50, MIRACULOUS_LADYBUGS_COUNT);
        Vec3 spawnPosition = entity.position();
        ArrayList<ArrayList<MiraculousLadybugTarget<?>>> targetDatas = assignTargets(level);

        int spawnedMLBCount;
        for (spawnedMLBCount = 0; spawnedMLBCount < MIRACULOUS_LADYBUGS_COUNT; spawnedMLBCount++) {
            ArrayList<MiraculousLadybugTarget<?>> targets = targetDatas.get(spawnedMLBCount);
            double x = circle.get(spawnedMLBCount).x;
            double y = circle.get(spawnedMLBCount).y;
            Vec3 circlePosition = spawnPosition.add(x, 0, y);
            targets = new ArrayList<>(MineraculousMathUtils.sortTargets(targets, circlePosition));
            MiraculousLadybugTargetData targetData = MiraculousLadybugTargetData.create(targets, spawnPosition, circlePosition);
            MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
            miraculousLadybug.setPos(spawnPosition);
            level.addFreshEntity(miraculousLadybug);
            miraculousLadybug.setTargetData(targetData);
        }
        level.sendParticles(ParticleTypes.FLASH, spawnPosition.x, spawnPosition.y, spawnPosition.z, 1, 0, 0, 0, 0);
    }

    private Collection<MiraculousLadybugTarget<?>> determineTargets(ServerLevel level) {
        AbilityReversionBlockData blockData = AbilityReversionBlockData.get(level);
        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
        AbilityReversionItemData itemData = AbilityReversionItemData.get(level);

        Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget> blockPositions = HashMultimap.create();
        Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> entityPositions = HashMultimap.create();
        for (UUID relatedId : collectToRevert(targetId, entityData)) {
            revertNotTargetable(level, relatedId, itemData);
            gatherReversionTargets(level, relatedId, blockPositions, entityPositions, blockData, entityData);
            // collect ability reversion targets
        }
        ResourceKey<Level> currentDimension = level.dimension();

        boolean reduceClumps = MineraculousServerConfig.get().miraculousLadybugReversionMode.get() == MineraculousServerConfig.MiraculousLadybugReversionMode.CLUSTER;
        Collection<MiraculousLadybugBlockTarget> allBlockTargets = blockPositions.removeAll(currentDimension);
        Collection<? extends MiraculousLadybugTarget<?>> blockTargets = reduceClumps ? MiraculousLadybugBlockClusterTarget.reduceNearbyBlocks(allBlockTargets) : allBlockTargets;
        Collection<MiraculousLadybugEntityTarget> entityTargets = entityPositions.removeAll(currentDimension);
        Collection<MiraculousLadybugTarget<?>> targets = new ReferenceOpenHashSet<>();
        targets.addAll(blockTargets);
        targets.addAll(entityTargets);
        revertInOtherDimensions(blockPositions, entityPositions, level);
        return targets;
    }

    private void revertNotTargetable(ServerLevel level, UUID relatedId, AbilityReversionItemData itemData) {
        itemData.markReverted(relatedId);
        Entity r = level.getEntity(relatedId);
        if (r instanceof LivingEntity related) {
            related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotizationData -> {
                Kamikotization value = kamikotizationData.kamikotization().value();
                AbilityData abilityData = AbilityData.of(kamikotizationData);
                value.powerSource().ifLeft(tool -> {
                    if (tool.getItem() instanceof EffectRevertingItem item) {
                        item.revert(related);
                    }
                }).ifRight(ability -> ability.value().revert(abilityData, level, related));
                value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
            });
            MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
                Miraculous value = miraculous.value();
                AbilityData abilityData = AbilityData.of(miraculousesData.get(miraculous));
                value.activeAbility().value().revert(abilityData, level, related);
                value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related));
            }
        }
    }

    private ArrayList<ArrayList<MiraculousLadybugTarget<?>>> assignTargets(ServerLevel level) {
        ArrayList<MiraculousLadybugTarget<?>> targets = new ArrayList<>(determineTargets(level));
        ArrayList<ArrayList<MiraculousLadybugTarget<?>>> targetsTable = new ArrayList<>(MIRACULOUS_LADYBUGS_COUNT);
        for (int i = 0; i < MIRACULOUS_LADYBUGS_COUNT; i++) {
            targetsTable.add(new ArrayList<>());
        }
        int targetsCount = targets.size() - 1;
        int miraculousLadybugIndex = 0;
        while (targetsCount > -1) {
            MiraculousLadybugTarget<?> target = targets.get(targetsCount);
            ArrayList<MiraculousLadybugTarget<?>> list = targetsTable.get(miraculousLadybugIndex);
            list.add(target);
            targetsTable.set(miraculousLadybugIndex, list);
            miraculousLadybugIndex = miraculousLadybugIndex == MIRACULOUS_LADYBUGS_COUNT - 1 ? 0 : miraculousLadybugIndex + 1;
            targetsCount--;
        }

        return targetsTable;
    }

    private void revertInOtherDimensions(
            Multimap<ResourceKey<Level>, ? extends MiraculousLadybugTarget<?>> blockTargets,
            Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> entityTargets,
            ServerLevel level) {
        Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> targetPositions = ArrayListMultimap.create();
        targetPositions.putAll(blockTargets);
        targetPositions.putAll(entityTargets);

        targetPositions.keySet().forEach(dimension -> {
            ServerLevel targetLevel = level.getServer().getLevel(dimension);
            if (targetLevel != null) {
                for (MiraculousLadybugTarget<?> target : targetPositions.removeAll(dimension)) {
                    target.revert(targetLevel, true);
                }
            } else {
                MineraculousConstants.LOGGER.error("Could not revert ability effects in dimension {} as it does not exist", dimension);
            }
        });
    }

    private static void gatherReversionTargets(
            ServerLevel level,
            UUID relatedId,
            Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget> blockTargets,
            Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> nonClumpableTargets,
            AbilityReversionBlockData blockData,
            AbilityReversionEntityData entityData) {
        for (Map.Entry<ResourceKey<Level>, BlockPos> entry : blockData.getReversionPositions(relatedId).entries()) {
            ResourceKey<Level> dimension = entry.getKey();
            BlockPos pos = entry.getValue();
            blockTargets.put(dimension, new MiraculousLadybugBlockTarget(pos, relatedId));
        }
        for (Map.Entry<ResourceKey<Level>, Vec3> location : entityData.getReversionAndConversionPositions(relatedId).entries()) {
            ResourceKey<Level> dimension = location.getKey();
            Vec3 pos = location.getValue();
            for (Map.Entry<UUID, CompoundTag> entry : entityData.getRevertibleAndConvertedAt(relatedId, dimension, pos).entrySet()) {
                UUID entityId = entry.getKey();
                Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                if (entity != null) {
                    nonClumpableTargets.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, entity.getBbWidth(), entity.getBbHeight()));
                } else {
                    CompoundTag tag = entry.getValue();
                    EntityType.by(tag).ifPresentOrElse(type -> nonClumpableTargets.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, type.getWidth(), type.getHeight())), () -> MineraculousConstants.LOGGER.error("Invalid entity data passed to RevertLuckyCharmTargetsAbilityEffectsAbility: {}", tag));
                }
            }
        }
    }

    private Set<UUID> collectToRevert(UUID uuid, AbilityReversionEntityData entityData) {
        Set<UUID> toRevert = new ReferenceOpenHashSet<>();
        toRevert.add(uuid);
        for (UUID related : entityData.getAndClearTrackedAndRelatedEntities(uuid)) {
            if (!toRevert.contains(related)) {
                toRevert.add(related);
                collectToRevert(related, entityData);
            }
        }
        return toRevert;
    }
    @FunctionalInterface
    public interface TargetCollector<T extends MiraculousLadybugTarget<T>> {
        void put(ResourceKey<Level> dimension, MiraculousLadybugTarget<T> target);
    }
}
