package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionBlockData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
                int max = Math.max(data.powerLevel(), 1) * 100;
                AbilityReversionBlockData.get(level).putRecoverable(performer.getUUID(), replaceBlockAndAdjacent(level, pos, max, new Object2ObjectOpenHashMap<>()));
            }
            return true;
        }
        return false;
    }

    private boolean canBlockBeReplaced(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() && validBlocks.map(predicate -> predicate.matches(level, pos)).orElse(true) && invalidBlocks.map(predicate -> !predicate.matches(level, pos)).orElse(false);
    }

    private Map<BlockPos, BlockState> replaceBlockAndAdjacent(ServerLevel level, BlockPos pos, int max, Map<BlockPos, BlockState> blocksAffected) {
        if (blocksAffected.size() >= max || blocksAffected.containsKey(pos) || !canBlockBeReplaced(level, pos)) {
            return blocksAffected;
        }

        // TODO: Spreading like water, preferring same blocks if boolean
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, Entity performer) {
        AbilityReversionBlockData.get(level).recover(performer.getUUID(), level);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.RANDOM_SPREAD.get();
    }
}
