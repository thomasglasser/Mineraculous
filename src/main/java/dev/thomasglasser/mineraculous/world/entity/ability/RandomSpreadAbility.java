package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryBlockData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record RandomSpreadAbility(BlockState blockState, Optional<BlockPredicate> validBlocks, Optional<BlockPredicate> invalidBlocks, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<RandomSpreadAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("state", Blocks.AIR.defaultBlockState()).forGetter(RandomSpreadAbility::blockState),
            BlockPredicate.CODEC.optionalFieldOf("valid_blocks").forGetter(RandomSpreadAbility::validBlocks),
            BlockPredicate.CODEC.optionalFieldOf("invalid_blocks").forGetter(RandomSpreadAbility::invalidBlocks),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(RandomSpreadAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(RandomSpreadAbility::overrideActive)).apply(instance, RandomSpreadAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.INTERACT_BLOCK) {
            if (canBlockBeReplaced(level, pos)) {
                Map<BlockPos, BlockState> blocksAffected = new HashMap<>();
                applyToBlock(level, pos, data.powerLevel(), blocksAffected);
                MiraculousRecoveryBlockData.get(level).putRecoverable(entity.getUUID(), blocksAffected);
                playStartSound(level, pos);
            }
            return true;
        }
        return false;
    }

    private void applyToBlock(ServerLevel level, BlockPos pos, int powerLevel, Map<BlockPos, BlockState> blocksAffected) {
        if (blocksAffected.containsKey(pos) || blocksAffected.size() >= Math.max(powerLevel, 1) * 100 || (validBlocks.isPresent() && !validBlocks.get().matches(level, pos)) || (invalidBlocks.isPresent() && invalidBlocks.get().matches(level, pos)))
            return;

        blocksAffected.put(pos, level.getBlockState(pos));

        RandomSource randomSource = level.random;
        replaceBlocksWithin(randomSource.nextInt(4, 8), level, pos, blocksAffected);

        for (Direction direction : Direction.values()) {
            if (!level.getBlockState(pos.relative(direction)).canBeReplaced()) {
                applyToBlock(level, pos.relative(direction), powerLevel, blocksAffected);
            }
        }
    }

    private boolean canBlockBeReplaced(ServerLevel level, BlockPos newPos) {
        return (validBlocks.isEmpty() || validBlocks.get().matches(level, newPos)) && (invalidBlocks.isEmpty() || !invalidBlocks.get().matches(level, newPos));
    }

    private void replaceBlocksWithin(int radius, ServerLevel level, BlockPos pos, Map<BlockPos, BlockState> blocksAffected) {
        int iRange = level.random.nextInt(radius);
        for (int i = -iRange; i <= iRange; i++) {
            int jRange = level.random.nextInt(radius);
            for (int j = -jRange; j <= jRange; j++) {
                int kRange = level.random.nextInt(radius);
                for (int k = -kRange; k <= kRange; k++) {
                    BlockPos newPos = pos.offset(i, j, k);
                    if (canBlockBeReplaced(level, newPos) && level.getBlockEntity(newPos) == null && level.random.nextBoolean()) {
                        blocksAffected.put(newPos, level.getBlockState(newPos));
                        level.setBlock(newPos, blockState, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @Override
    public void restore(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        MiraculousRecoveryBlockData.get(level).recover(entity.getUUID(), level);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.RANDOM_SPREAD.get();
    }
}
