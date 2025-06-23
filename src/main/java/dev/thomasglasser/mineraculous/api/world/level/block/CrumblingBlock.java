package dev.thomasglasser.mineraculous.api.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/// Destroyed and dropped on touch or random tick.
public class CrumblingBlock extends Block {
    public CrumblingBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.destroyBlock(pos, true);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        level.destroyBlock(pos, true);
    }
}
