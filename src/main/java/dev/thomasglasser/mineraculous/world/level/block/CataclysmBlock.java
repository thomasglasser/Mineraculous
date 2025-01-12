package dev.thomasglasser.mineraculous.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CataclysmBlock extends ColoredFallingBlock {
    public CataclysmBlock(Properties properties) {
        super(new ColorRGBA(0x201915), properties.randomTicks());
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
