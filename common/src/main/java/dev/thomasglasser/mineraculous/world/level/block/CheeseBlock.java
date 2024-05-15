package dev.thomasglasser.mineraculous.world.level.block;

import com.mojang.serialization.Codec;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;
import java.util.Optional;

public class CheeseBlock extends Block implements ChangeOverTimeBlock<CheeseBlock.Age> {
    public final Age age;
    public final boolean waxed;

    public CheeseBlock(Age age, boolean waxed, Properties properties) {
        super(properties);
        this.age = age;
        this.waxed = waxed;
    }

    /**
     * Override to set map for cheese oxidation and unwaxing.
     */
    public Map<Age,RegistryObject<CheeseBlock>> cheeseMap() {
        return MineraculousBlocks.CHEESE_BLOCK;
    }

    /**
     * Override to set map for cheese waxing.
     */
    public Map<Age,RegistryObject<CheeseBlock>> waxMap() {
        return MineraculousBlocks.WAXED_CHEESE_BLOCK;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!waxed && stack.is(Items.HONEYCOMB)) level.setBlock(pos, waxMap().get(age).get().defaultBlockState(), 2);
        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // No change if waxed.
        if (!waxed) changeOverTime(state,level,pos,random);
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        if (Age.TIME_HONORED.equals(age)) return Optional.empty();
        return Optional.of(cheeseMap().get(switch (age) {
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
