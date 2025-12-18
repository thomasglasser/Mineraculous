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
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugClusterTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugEntityTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public record MiraculousLadybugTriggerData(UUID performerId, UUID targetId, Optional<Holder<SoundEvent>> revertSound, int tickCount) {

    public static final Codec<MiraculousLadybugTriggerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("performer_id").forGetter(MiraculousLadybugTriggerData::performerId),
            UUIDUtil.CODEC.fieldOf("target_id").forGetter(MiraculousLadybugTriggerData::targetId),
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(MiraculousLadybugTriggerData::revertSound),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("tick_count").forGetter(MiraculousLadybugTriggerData::tickCount)).apply(instance, MiraculousLadybugTriggerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTriggerData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MiraculousLadybugTriggerData::performerId,
            UUIDUtil.STREAM_CODEC, MiraculousLadybugTriggerData::targetId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), MiraculousLadybugTriggerData::revertSound,
            ByteBufCodecs.VAR_INT, MiraculousLadybugTriggerData::tickCount,
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
            this.incrementTicks().save(entity);
            entity.setNoGravity(true);
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
            if (tickCount == 1) {
                LivingEntity performer = this.getPerformer(level);
                if (performer != null)
                    Ability.playSound(level, performer, revertSound);
                spawnSphereParticles(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), level, entity.position(), 200);
                spawnSphereParticles(ParticleTypes.END_ROD, level, entity.position(), 10);
            } else if (tickCount > 20) {
                spawnMiraculousLadybugs(level, entity);
                LuckyCharmIdData.get(level).incrementLuckyCharmId(performerId);
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
        List<Vector2d> circle = MineraculousMathUtils.generateCirclePoints(50, MIRACULOUS_LADYBUGS_COUNT);
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

    private List<MiraculousLadybugTarget<?>> determineTargets(ServerLevel level) {
        BlockReversionData blockData = BlockReversionData.get(level);
        EntityReversionData entityData = EntityReversionData.get(level);
        ItemReversionData itemData = ItemReversionData.get(level);

        Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> nonClusterableTargets = HashMultimap.create();
        Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> clusterableTargets = HashMultimap.create();
        MiraculousLadybugTargetCollector collector = MiraculousLadybugTargetCollector.of(nonClusterableTargets::put, clusterableTargets::put);
        for (UUID relatedId : collectToRevert(targetId, entityData)) {
            beginReversionAndGatherTargets(level, relatedId, collector, blockData, entityData, itemData);
            if (level.getEntity(relatedId) instanceof LivingEntity related) {
                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotizationData -> {
                    Kamikotization value = kamikotizationData.kamikotization().value();
                    AbilityData abilityData = AbilityData.of(kamikotizationData);
                    value.powerSource().ifLeft(tool -> {
                        if (tool.getItem() instanceof EffectRevertingItem item) {
                            item.revert(related, collector);
                        }
                    }).ifRight(ability -> ability.value().revert(abilityData, level, related, collector));
                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, collector));
                });
                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
                    Miraculous value = miraculous.value();
                    AbilityData abilityData = AbilityData.of(miraculousesData.get(miraculous));
                    value.activeAbility().value().revert(abilityData, level, related, collector);
                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, collector));
                }
            }
        }

        ResourceKey<Level> currentDimension = level.dimension();
        boolean shouldCluster = MineraculousServerConfig.get().miraculousLadybugReversionMode.get() == MineraculousServerConfig.MiraculousLadybugReversionMode.CLUSTERED;
        Collection<MiraculousLadybugTarget<?>> unclusteredTargets = clusterableTargets.removeAll(currentDimension);
        Collection<MiraculousLadybugTarget<?>> clusteredTargets = shouldCluster ? MiraculousLadybugClusterTarget.reduceNearbyTargets(unclusteredTargets) : unclusteredTargets;
        List<MiraculousLadybugTarget<?>> dimensionTargets = new ReferenceArrayList<>(nonClusterableTargets.removeAll(currentDimension));
        dimensionTargets.addAll(clusteredTargets);
        revertInOtherDimensions(nonClusterableTargets, clusterableTargets, level);
        return dimensionTargets;
    }

    private ArrayList<ArrayList<MiraculousLadybugTarget<?>>> assignTargets(ServerLevel level) {
        List<MiraculousLadybugTarget<?>> targets = determineTargets(level);
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
            Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> nonClusterableTargets,
            Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> clusterableTargets,
            ServerLevel level) {
        Multimap<ResourceKey<Level>, MiraculousLadybugTarget<?>> targets = ArrayListMultimap.create();
        targets.putAll(nonClusterableTargets);
        targets.putAll(clusterableTargets);

        targets.keySet().forEach(dimension -> {
            ServerLevel targetLevel = level.getServer().getLevel(dimension);
            if (targetLevel != null) {
                for (MiraculousLadybugTarget<?> target : targets.removeAll(dimension)) {
                    target.revert(targetLevel, true);
                }
            } else {
                MineraculousConstants.LOGGER.error("Could not revert ability effects in dimension {} as it does not exist", dimension);
            }
        });
    }

    // TODO: Move to event
    private static void beginReversionAndGatherTargets(
            ServerLevel level,
            UUID relatedId,
            MiraculousLadybugTargetCollector targetCollector,
            BlockReversionData blockData,
            EntityReversionData entityData,
            ItemReversionData itemData) {
        itemData.markReverted(relatedId);
        entityData.revertRemovableAndCopied(relatedId, level);
        for (Map.Entry<ResourceKey<Level>, BlockPos> location : blockData.getReversionPositions(relatedId).entries()) {
            targetCollector.putClusterable(location.getKey(), new MiraculousLadybugBlockTarget(location.getValue(), relatedId));
        }
        for (Map.Entry<ResourceKey<Level>, Vec3> location : entityData.getReversionAndConversionPositions(relatedId).entries()) {
            ResourceKey<Level> dimension = location.getKey();
            Vec3 pos = location.getValue();
            for (Map.Entry<UUID, CompoundTag> entry : entityData.getRevertibleAndConvertedAt(relatedId, dimension, pos).entrySet()) {
                UUID entityId = entry.getKey();
                Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                if (entity != null) {
                    targetCollector.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, entity.getBbWidth(), entity.getBbHeight()));
                } else {
                    CompoundTag tag = entry.getValue();
                    EntityType.by(tag).ifPresentOrElse(type -> targetCollector.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, type.getWidth(), type.getHeight())), () -> MineraculousConstants.LOGGER.error("Invalid entity data passed to RevertLuckyCharmTargetsAbilityEffectsAbility: {}", tag));
                }
            }
        }
    }

    private Set<UUID> collectToRevert(UUID uuid, EntityReversionData entityData) {
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
}
