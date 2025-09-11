package dev.thomasglasser.mineraculous.api.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/// A {@link PieceBlock} with 4 pieces and a smaller shape
public class CheeseFullBlock extends PieceBlock {
    public static final int MAX_PIECES = 4;
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 3.0, 12.0);

    public CheeseFullBlock(Holder<Item> piece, Properties properties) {
        super(MAX_PIECES, piece, properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
