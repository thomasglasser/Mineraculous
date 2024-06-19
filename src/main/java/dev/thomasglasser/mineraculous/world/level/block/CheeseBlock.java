package dev.thomasglasser.mineraculous.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Supplier;

public class CheeseBlock extends Block implements ChangeOverTimeBlock<CheeseBlock.Age> {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(
            () -> {
                 ImmutableBiMap.Builder<Block, Block> builder = ImmutableBiMap.builder();
                 MineraculousBlocks.CHEESE_BLOCKS.forEach((age, block) -> builder.put(block.get(), MineraculousBlocks.WAXED_CHEESE_BLOCKS.get(age).get()));
                 MineraculousBlocks.CAMEMBERT_BLOCKS.forEach((age, block) -> builder.put(block.get(), MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.get(age).get()));
                 return builder.build();
            }
    );
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public static final int MAX_BITES = 3;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, MAX_BITES);
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 3.0, 12.0);

    private final Age age;
    private final boolean waxed;
    private final Map<Age, DeferredBlock<CheeseBlock>> cheeseMap;
    private final Map<Age, DeferredItem<?>> wedgeMap;
    private final FoodProperties foodProperties;

    public CheeseBlock(Age age, boolean waxed, SortedMap<Age, DeferredBlock<CheeseBlock>> stages, SortedMap<Age, DeferredItem<?>> wedgeMap, FoodProperties foodProperties, Properties properties) {
        super(properties);
        this.age = age;
        this.waxed = waxed;
        this.cheeseMap = stages;
        this.wedgeMap = wedgeMap;
        this.foodProperties = foodProperties;
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // No change if waxed.
        if (!waxed) changeOverTime(state,level,pos,random);
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        if (Age.TIME_HONORED.equals(age)) return Optional.empty();
        return Optional.of(cheeseMap.get(switch (age) {
            case FRESH -> Age.AGED;
            case AGED -> Age.RIPENED;
            case RIPENED -> Age.EXQUISITE;
            case EXQUISITE -> Age.TIME_HONORED;
            default -> Age.FRESH; // This should never happen.
        }).get().defaultBlockState());
    }

    @Override
    public float getChanceModifier() {
        return 1;
    }

    @Override
    public Age getAge() {
        return age;
    }

    public static int getOutputSignal(int eaten) {
        return (4 - eaten) * 2;
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            if (eat(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        return eat(level, pos, state, player);
    }

    protected InteractionResult eat(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false) || waxed) {
            return InteractionResult.PASS;
        } else {
            player.awardStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
            int i = state.getValue(BITES);
            level.gameEvent(player, GameEvent.EAT, pos);
            if (i < MAX_BITES) {
                level.setBlock(pos, state.setValue(BITES, i + 1), Block.UPDATE_ALL);
            } else {
                level.removeBlock(pos, false);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }

            return InteractionResult.SUCCESS;
        }
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getOutputSignal(state.getValue(BITES));
    }

    public DeferredItem<?> getWedge(Age age)
    {
        return wedgeMap.get(age);
    }

    public boolean isWaxed()
    {
        return waxed;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate)
    {
        Block block = CheeseBlock.WAX_OFF_BY_BLOCK.get().get(this);
        if (toolAction == ToolActions.AXE_WAX_OFF && block != null)
        {
            return block.defaultBlockState();
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }

    public enum Age implements StringRepresentable {
        FRESH,
        AGED,
        RIPENED,
        EXQUISITE,
        TIME_HONORED;

        public static final Codec<Age> CODEC = StringRepresentable.fromEnum(Age::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}