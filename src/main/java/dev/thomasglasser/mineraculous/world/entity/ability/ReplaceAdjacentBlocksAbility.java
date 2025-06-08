package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionBlockData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record ReplaceAdjacentBlocksAbility(BlockState replacement, boolean preferSame, Optional<BlockPredicate> validBlocks, Optional<BlockPredicate> invalidBlocks) implements Ability {

    public static final MapCodec<ReplaceAdjacentBlocksAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("replacement", Blocks.AIR.defaultBlockState()).forGetter(ReplaceAdjacentBlocksAbility::replacement),
            Codec.BOOL.optionalFieldOf("prefer_same", true).forGetter(ReplaceAdjacentBlocksAbility::preferSame),
            BlockPredicate.CODEC.optionalFieldOf("valid_blocks").forGetter(ReplaceAdjacentBlocksAbility::validBlocks),
            BlockPredicate.CODEC.optionalFieldOf("invalid_blocks").forGetter(ReplaceAdjacentBlocksAbility::invalidBlocks)).apply(instance, ReplaceAdjacentBlocksAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context instanceof BlockAbilityContext(BlockPos pos)) {
            if (canBlockBeReplaced(level, pos)) {
                Set<BlockPos> affected = getAffectedBlocks(level, pos, Math.max(data.powerLevel(), 1) * 100);
                Map<BlockPos, BlockState> originals = new Object2ObjectOpenHashMap<>();
                for (BlockPos blockPos : affected) {
                    BlockState original = level.getBlockState(blockPos);
                    originals.put(blockPos, original);
                    level.setBlock(blockPos, MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                AbilityReversionBlockData.get(level).putRecoverable(performer.getUUID(), originals);
            }
            return true;
        }
        return false;
    }

    private boolean canBlockBeReplaced(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() && validBlocks.map(predicate -> predicate.matches(level, pos)).orElse(true) && invalidBlocks.map(predicate -> !predicate.matches(level, pos)).orElse(false);
    }

    private Set<BlockPos> getAffectedBlocks(ServerLevel level, BlockPos pos, int max) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        Set<BlockPos> adjacent = new ObjectOpenHashSet<>();
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
            BlockState state = level.getBlockState(current);
            if (adjacent.size() < 100 && visited.add(current) && !state.isAir() && (!requireSame || state.is(block))) {
                adjacent.add(current);
                for (Direction direction : Direction.values()) {
                    BlockPos relative = current.relative(direction);
                    if (!visited.contains(relative)) {
                        queue.add(relative);
                    }
                }
            }
        }
        return adjacent;
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, Entity performer) {
        AbilityReversionBlockData.get(level).recover(performer.getUUID(), level);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REPLACE_ADJACENT_BLOCKS.get();
    }
}
