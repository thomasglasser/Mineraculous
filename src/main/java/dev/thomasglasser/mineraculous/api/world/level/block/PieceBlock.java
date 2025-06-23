package dev.thomasglasser.mineraculous.api.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;

/// A block composed of pieces that can be missing
public class PieceBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PieceBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("max_pieces").forGetter(PieceBlock::getMaxPieces),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("piece").forGetter(PieceBlock::getPiece),
            propertiesCodec()).apply(instance, PieceBlock::new));
    public static final String MISSING_PIECES = "missing_pieces";

    private final int maxPieces;
    private final Holder<Item> piece;
    private final IntegerProperty missingPiecesProperty;

    public PieceBlock(int maxPieces, Holder<Item> piece, Properties properties) {
        super(properties);
        this.maxPieces = maxPieces;
        this.missingPiecesProperty = IntegerProperty.create(MISSING_PIECES, 0, maxPieces - 1);
        this.piece = piece;
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any().setValue(missingPiecesProperty, 0).setValue(FACING, Direction.NORTH));
    }

    public int getMaxPieces() {
        return maxPieces;
    }

    public int getMaxMissingPieces() {
        return maxPieces - 1;
    }

    public Holder<Item> getPiece() {
        return piece;
    }

    public IntegerProperty getMissingPiecesProperty() {
        return missingPiecesProperty;
    }

    @Override
    protected MapCodec<? extends PieceBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction opposite = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, opposite);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (missingPiecesProperty != null) {
            builder.add(missingPiecesProperty);
            builder.add(FACING);
        }
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return maxPieces - state.getValue(missingPiecesProperty);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (state.getValue(missingPiecesProperty) == getMaxMissingPieces()) {
            return piece.value().getDefaultInstance();
        } else {
            ItemStack cloneItemStack = super.getCloneItemStack(state, target, level, pos, player);
            cloneItemStack.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, properties -> {
                for (Property<?> property : state.getProperties()) {
                    if (state.hasProperty(property)) {
                        properties = properties.with(property, state);
                    }
                }

                return properties;
            });
            return cloneItemStack;
        }
    }
}
