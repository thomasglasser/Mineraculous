package dev.thomasglasser.mineraculous.api.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/// Both a {@link PieceBlock} with 4 pieces and an {@link AgeingCheese} that can be eaten or added to on interaction.
public class AgeingCheeseEdibleFullBlock extends PieceBlock implements AgeingCheese {
    public static final MapCodec<AgeingCheeseEdibleFullBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Age.CODEC.fieldOf("age").forGetter(AgeingCheeseEdibleFullBlock::getAge),
            FoodProperties.DIRECT_CODEC.fieldOf("food_properties").forGetter(AgeingCheeseEdibleFullBlock::getFoodProperties),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("wedge").forGetter(AgeingCheeseEdibleFullBlock::getPiece),
            propertiesCodec()).apply(instance, AgeingCheeseEdibleFullBlock::new));
    public static final int MAX_PIECES = 4;
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 3.0, 12.0);

    protected final Age age;
    protected final FoodProperties foodProperties;

    public AgeingCheeseEdibleFullBlock(Age age, FoodProperties foodProperties, Holder<Item> wedge, BlockBehaviour.Properties properties) {
        super(MAX_PIECES, wedge, properties);
        this.age = age;
        this.foodProperties = foodProperties;
    }

    @Override
    protected MapCodec<? extends AgeingCheeseEdibleFullBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int bites = state.getValue(getMissingPiecesProperty());
        if (stack.is(getPiece()) && bites > 0) {
            player.playSound(getSoundType(state, level, pos, player).getPlaceSound());
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(getMissingPiecesProperty(), bites - 1), Block.UPDATE_ALL);
                ItemUtils.safeShrink(stack, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.canEat(false)) {
            player.playSound(SoundEvents.GENERIC_EAT);
            if (!level.isClientSide) {
                player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
                level.gameEvent(player, GameEvent.EAT, pos);
                int i = state.getValue(getMissingPiecesProperty());
                if (i < getMaxMissingPieces()) {
                    level.setBlock(pos, state.setValue(getMissingPiecesProperty(), i + 1), Block.UPDATE_ALL);
                } else {
                    level.removeBlock(pos, false);
                    level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return AgeingCheese.getNext(state.getBlock()).isPresent();
    }

    public Age getAge() {
        return age;
    }

    public FoodProperties getFoodProperties() {
        return foodProperties;
    }
}
