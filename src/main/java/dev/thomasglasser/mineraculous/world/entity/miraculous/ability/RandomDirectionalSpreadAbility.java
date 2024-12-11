package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.Optional;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record RandomDirectionalSpreadAbility(BlockState blockState, Optional<BlockPredicate> validBlocks, Optional<BlockPredicate> invalidBlocks, Optional<Holder<SoundEvent>> startSound) implements Ability {

    public static final MapCodec<RandomDirectionalSpreadAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("state", Blocks.AIR.defaultBlockState()).forGetter(RandomDirectionalSpreadAbility::blockState),
            BlockPredicate.CODEC.optionalFieldOf("valid_blocks").forGetter(RandomDirectionalSpreadAbility::validBlocks),
            BlockPredicate.CODEC.optionalFieldOf("immune_blocks").forGetter(RandomDirectionalSpreadAbility::invalidBlocks),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(RandomDirectionalSpreadAbility::startSound)).apply(instance, RandomDirectionalSpreadAbility::new));
    @Override
    public boolean perform(AbilityData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_BLOCK) {
            applyToBlock(level, pos, data.powerLevel(), 0, null);
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    private void applyToBlock(Level level, BlockPos pos, int powerLevel, int blocksAffected, @Nullable Direction nextPosDirection) {
        if (level instanceof ServerLevel serverLevel) {
            if ((validBlocks.isPresent() && !validBlocks.get().matches(serverLevel, pos)) || (invalidBlocks.isPresent() && invalidBlocks.get().matches(serverLevel, pos)) || blocksAffected >= Math.max(powerLevel, 1) * 100)
                return;
            blocksAffected++;

            int range = 3;
            for (int i = -range; i <= range; i++) {
                for (int j = -range; j <= range; j++) {
                    BlockPos newPos = pos.offset(i, 0, j);
                    BlockState newState = level.getBlockState(newPos);
                    if (newState.is(BlockTags.LOGS) || newState.is(BlockTags.LEAVES) || newState.getBlock() instanceof MangroveRootsBlock) {
                        level.setBlock(newPos, MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }

            if (nextPosDirection == null) {
                nextPosDirection = switch (level.random.nextInt(5)) {
                    case 0 -> Direction.NORTH;
                    case 1 -> Direction.EAST;
                    case 2 -> Direction.SOUTH;
                    case 3 -> Direction.WEST;
                    default -> Direction.UP;
                };
            }

            if (!level.getBlockState(pos.relative(nextPosDirection)).canBeReplaced()) {
                applyToBlock(level, pos.relative(nextPosDirection), powerLevel, blocksAffected, nextPosDirection);
            }

            RandomSource randomSource = level.random;
            replaceBlocksWithin(randomSource.nextInt(4, 8), serverLevel, pos);
        }
    }

    private void replaceBlocksWithin(int radius, ServerLevel level, BlockPos pos) {
        int iRange = level.random.nextInt(radius);
        for (int i = -iRange; i <= iRange; i++) {
            int jRange = level.random.nextInt(radius);
            for (int j = -jRange; j <= jRange; j++) {
                int kRange = level.random.nextInt(radius);
                for (int k = -kRange; k <= kRange; k++) {
                    BlockPos newPos = pos.offset(i, j, k);
                    if ((validBlocks.isEmpty() || validBlocks.get().matches(level, newPos)) && (invalidBlocks.isEmpty() || !invalidBlocks.get().matches(level, newPos)) && level.random.nextBoolean()) {
                        level.setBlock(newPos, blockState, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.RANDOM_DIRECTIONAL_SPREAD.get();
    }
}
