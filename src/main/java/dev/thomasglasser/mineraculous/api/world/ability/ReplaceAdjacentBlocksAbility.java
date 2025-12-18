package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockLocation;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.PersistentAbilityEffectData;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Replaces adjacent blocks with the provided {@link BlockState} in a diamond shape,
 * preferring the same {@link Block} when enabled.
 *
 * @param replacement   The {@link BlockState} to replace the target blocks with
 * @param preferSame    Whether the algorithm should choose blocks of the same kind as the target when adjacent
 * @param validBlocks   The {@link BlockPredicate} the target must match
 * @param invalidBlocks The {@link BlockPredicate} the target must not match
 * @param replaceSound  The sound to play when the blocks are replaced
 */
public record ReplaceAdjacentBlocksAbility(BlockState replacement, boolean instant, boolean preferSame, Optional<BlockPredicate> validBlocks, Optional<BlockPredicate> invalidBlocks, Optional<Holder<SoundEvent>> replaceSound) implements Ability {

    public static final MapCodec<ReplaceAdjacentBlocksAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("replacement", Blocks.AIR.defaultBlockState()).forGetter(ReplaceAdjacentBlocksAbility::replacement),
            Codec.BOOL.optionalFieldOf("instant", true).forGetter(ReplaceAdjacentBlocksAbility::instant),
            Codec.BOOL.optionalFieldOf("prefer_same", true).forGetter(ReplaceAdjacentBlocksAbility::preferSame),
            BlockPredicate.CODEC.optionalFieldOf("valid_blocks").forGetter(ReplaceAdjacentBlocksAbility::validBlocks),
            BlockPredicate.CODEC.optionalFieldOf("invalid_blocks").forGetter(ReplaceAdjacentBlocksAbility::invalidBlocks),
            SoundEvent.CODEC.optionalFieldOf("replace_sound").forGetter(ReplaceAdjacentBlocksAbility::replaceSound)).apply(instance, ReplaceAdjacentBlocksAbility::new));
    /**
     * Replaces the block at the specified {@link BlockLocation} with the specified replacement.
     *
     * @param affected    The location to replace
     * @param replacement The replacement block
     * @param cause       The cause ID of the replacement
     * @param level       The level to fetch the server from
     */
    public static void replace(BlockLocation affected, BlockState replacement, UUID cause, ServerLevel level) {
        level = level.getServer().getLevel(affected.dimension());
        if (level != null) {
            BlockReversionData.get(level).putRevertible(cause, affected.dimension(), affected.pos(), level.getBlockState(affected.pos()));
            level.setBlock(affected.pos(), replacement, Block.UPDATE_ALL);
        } else {
            MineraculousConstants.LOGGER.error("Could not replace block at {} in dimension {} because the level does not exist", affected.pos(), affected.dimension());
        }
    }

    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context instanceof BlockAbilityContext(BlockPos pos)) {
            if (!isValidBlock(level, pos)) {
                return State.CANCEL;
            }
            ImmutableSet<BlockPos> affected = getAffectedBlocks(level, pos, Math.max((int) Math.ceil(data.powerLevel() / 10.0), 1) * 100);
            if (instant) {
                affected.forEach(affectedPos -> replace(new BlockLocation(level.dimension(), affectedPos), replacement, performer.getUUID(), level));
            } else {
                ImmutableSet.Builder<PersistentAbilityEffectData.DelayedBlockReplacement> replacements = new ImmutableSet.Builder<>();
                for (BlockPos affectedPos : affected) {
                    replacements.add(new PersistentAbilityEffectData.DelayedBlockReplacement(new BlockLocation(level.dimension(), affectedPos), replacement));
                }
                performer.getData(MineraculousAttachmentTypes.PERSISTENT_ABILITY_EFFECTS).addDelayedBlockReplacements(replacements.build()).save(performer);
            }
            Ability.playSound(level, performer, replaceSound);
            return State.CONSUME;
        }
        return State.PASS;
    }

    /**
     * Checks if the provided position is valid for the ability.
     *
     * @param level The level the position is in
     * @param pos   The position of the block
     * @return Whether the block is valid for the ability
     */
    public boolean isValidBlock(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() && validBlocks.map(predicate -> predicate.matches(level, pos)).orElse(true) && invalidBlocks.map(predicate -> !predicate.matches(level, pos)).orElse(true);
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, LivingEntity performer, MiraculousLadybugTargetCollector targetCollector) {
        performer.getData(MineraculousAttachmentTypes.PERSISTENT_ABILITY_EFFECTS).withDelayedBlockReplacements(ImmutableSet.of()).save(performer);
    }

    private ImmutableSet<BlockPos> getAffectedBlocks(ServerLevel level, BlockPos pos, int max) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        ImmutableSet.Builder<BlockPos> adjacent = new ImmutableSet.Builder<>();
        int affected = 0;
        queue.add(pos);
        boolean requireSame = false;
        Block block = level.getBlockState(pos).getBlock();
        if (preferSame) {
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(block)) {
                    requireSame = true;
                }
            }
        }
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (affected < max && visited.add(current) && isValidBlock(level, current) && (!requireSame || level.getBlockState(current).is(block))) {
                affected++;
                adjacent.add(current);
                for (Direction direction : Direction.values()) {
                    BlockPos relative = current.relative(direction);
                    if (!visited.contains(relative)) {
                        queue.add(relative);
                    }
                }
            }
        }
        return adjacent.build();
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REPLACE_ADJACENT_BLOCKS.get();
    }
}
