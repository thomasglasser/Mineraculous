package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.impl.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugEntityTarget;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTriggerData;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Reverts the ability effects of the {@link LuckyCharm} target and related entities.
 *
 * @param revertSound The sound to play when reverting ability effects
 */
public record RevertLuckyCharmTargetsAbilityEffectsAbility(Optional<Holder<SoundEvent>> revertSound) implements Ability {
    public static final MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(RevertLuckyCharmTargetsAbilityEffectsAbility::revertSound)).apply(instance, RevertLuckyCharmTargetsAbilityEffectsAbility::new));

    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        boolean validUsage = isValidLuckyCharmUse(data, context, performer, handler);
        if (validUsage) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            UUID target = luckyCharm.target().orElse(luckyCharm.owner());
            AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
            Set<UUID> toRevert = collectToRevert(target, entityData);
            Pair<Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget>, Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget>> positions = gatherReversionPositions(level, toRevert);

            Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget> blockPositions = positions.first;
            Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> entityPositions = positions.second;
            ResourceKey<Level> currentDimension = level.dimension();

            boolean reduceClumps = MineraculousServerConfig.get().enableMLBClumpDetection.get();
            Collection<MiraculousLadybugBlockTarget> blocksInThisDimension = blockPositions.removeAll(currentDimension);
            Collection<MiraculousLadybugBlockTarget> blockTargets = reduceClumps ? MineraculousMathUtils.reduceNearbyBlocks(blocksInThisDimension) : blocksInThisDimension;
            Collection<MiraculousLadybugEntityTarget> entityTargets = entityPositions.removeAll(currentDimension);
            revertInOtherDimensions(blockPositions, entityPositions, level);

            ItemEntity luckyCharmEntity = new ItemEntity(level, performer.getX(), performer.getY() + 2, performer.getZ(), stack.copy());
            luckyCharmEntity.setNeverPickUp();
            luckyCharmEntity.setUnlimitedLifetime();
            level.addFreshEntity(luckyCharmEntity);
            stack.setCount(0);
            luckyCharmEntity.setDeltaMovement(0, 1.3, 0);
            luckyCharmEntity.hurtMarked = true;
            new MiraculousLadybugTriggerData(blockTargets, entityTargets, Optional.of(performer.getId()), revertSound).save(luckyCharmEntity, true);
            return State.SUCCESS;
        }
        return State.FAIL;
    }

    private void revertInOtherDimensions(
            Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget> blockPositions,
            Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> entityPositions,
            ServerLevel level) {
        for (ResourceKey<Level> dimension : blockPositions.keySet()) {
            ServerLevel targetLevel = level.getServer().getLevel(dimension);
            if (targetLevel != null) {
                Collection<MiraculousLadybugBlockTarget> blocks = new ArrayList<>(blockPositions.get(dimension));
                blockPositions.removeAll(dimension);
                // TODO: Fancy effects for reversion
                for (MiraculousLadybugBlockTarget blockTarget : blocks) {
                    MiraculousLadybugBlockTarget newTarget = blockTarget.revert(targetLevel);
                    blockPositions.put(dimension, newTarget);
                }
            } else {
                MineraculousConstants.LOGGER.error("Could not revert block ability effects in dimension {} as it does not exist", dimension);
            }
        }

        for (ResourceKey<Level> dimension : entityPositions.keySet()) {
            ServerLevel targetLevel = level.getServer().getLevel(dimension);
            if (targetLevel != null) {
                Collection<MiraculousLadybugEntityTarget> entities = new ArrayList<>(entityPositions.get(dimension));
                entityPositions.removeAll(dimension);
                // TODO: Fancy effects for reversion
                for (MiraculousLadybugEntityTarget entityTarget : entities) {
                    MiraculousLadybugEntityTarget newTarget = entityTarget.revert(targetLevel);
                    entityPositions.put(dimension, newTarget);
                }
            } else {
                MineraculousConstants.LOGGER.error("Could not revert entity ability effects in dimension {} as it does not exist", dimension);
            }
        }
    }

    private static Pair<Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget>, Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget>> gatherReversionPositions(ServerLevel level, Set<UUID> toRevert) {
        Multimap<ResourceKey<Level>, MiraculousLadybugBlockTarget> blockPositions = HashMultimap.create();
        Multimap<ResourceKey<Level>, MiraculousLadybugEntityTarget> entityPositions = HashMultimap.create();
        AbilityReversionBlockData blockData = AbilityReversionBlockData.get(level);
        AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
        for (UUID relatedId : toRevert) {
            for (Map.Entry<ResourceKey<Level>, BlockPos> entry : blockData.getReversionPositions(relatedId).entries()) {
                ResourceKey<Level> dimension = entry.getKey();
                BlockPos pos = entry.getValue();
                blockPositions.put(dimension, new MiraculousLadybugBlockTarget(pos, relatedId));
            }
            for (Map.Entry<ResourceKey<Level>, Vec3> entry : entityData.getReversionPositions(relatedId).entries()) {
                ResourceKey<Level> dimension = entry.getKey();
                Vec3 pos = entry.getValue();
                for (CompoundTag tag : entityData.getRevertibleAt(relatedId, dimension, pos)) {
                    UUID entityId = tag.getUUID("UUID");
                    Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                    if (entity != null) {
                        entityPositions.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, entity.getBbWidth(), entity.getBbHeight()));
                    } else {
                        EntityType.by(tag).ifPresentOrElse(type -> entityPositions.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, type.getWidth(), type.getHeight())), () -> MineraculousConstants.LOGGER.error("Invalid entity data passed to RevertLuckyCharmTargetsAbilityEffectsAbility: {}", tag));
                    }
                }
            }
        }
        return Pair.of(blockPositions, entityPositions);
    }

    private static boolean isValidLuckyCharmUse(AbilityData data, @Nullable AbilityContext context, LivingEntity performer, AbilityHandler handler) {
        if (context == null && data.powerActive()) {
            ItemStack stack = performer.getMainHandItem();
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                UUID performerId = handler.getMatchingBlame(stack, performer);
                return luckyCharm.owner().equals(performerId);
            }
        }
        return false;
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

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REVERT_LUCKY_CHARM_TARGETS_ABILITY_EFFECTS.get();
    }
}
