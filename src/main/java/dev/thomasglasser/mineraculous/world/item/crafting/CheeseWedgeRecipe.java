package dev.thomasglasser.mineraculous.world.item.crafting;

import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.tommylib.api.world.item.BlockStateItem;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class CheeseWedgeRecipe extends CustomRecipe {
    public CheeseWedgeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> items = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
        if (items.size() == 1) {
            ItemStack only = items.getFirst();
            BlockItemStateProperties properties = only.get(DataComponents.BLOCK_STATE);
            Integer bites = properties != null ? properties.get(CheeseBlock.BITES) : null;
            if (bites == null && only.getItem() instanceof BlockStateItem blockStateItem)
                bites = blockStateItem.getState().getValue(CheeseBlock.BITES);
            return only.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed() && (bites == null || bites < CheeseBlock.MAX_BITES);
        } else {
            ItemStack first = items.getFirst();
            return items.stream().allMatch(stack -> {
                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed() && stack.is(first.getItem())) {
                    BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
                    Integer bites = properties != null ? properties.get(CheeseBlock.BITES) : null;
                    if (bites == null && stack.getItem() instanceof BlockStateItem blockStateItem)
                        bites = blockStateItem.getState().getValue(CheeseBlock.BITES);
                    return bites != null && bites >= CheeseBlock.MAX_BITES;
                }
                return false;
            });
        }
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> items = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
        if (items.size() == 1) {
            ItemStack only = items.getFirst();
            BlockItemStateProperties properties = only.get(DataComponents.BLOCK_STATE);
            Integer bites = properties != null ? properties.get(CheeseBlock.BITES) : 0;
            if (bites == null) bites = 0;
            return new ItemStack(((CheeseBlock) ((BlockItem) only.getItem()).getBlock()).getWedge().get(), 4 - bites);
        } else {
            CheeseBlock cheeseBlock = ((BlockItem) items.getFirst().getItem()).getBlock() instanceof CheeseBlock block ? block : null;
            if (cheeseBlock != null) {
                int bites = items.size();
                BlockState state = cheeseBlock.defaultBlockState().setValue(CheeseBlock.BITES, 4 - bites);
                ItemStack result = cheeseBlock.asItem().getDefaultInstance();
                result.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, p_330174_ -> {
                    for (Property<?> property : state.getProperties()) {
                        if (state.hasProperty(property)) {
                            p_330174_ = p_330174_.with(property, state);
                        }
                    }

                    return p_330174_;
                });
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w + h >= 1;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return MineraculousRecipeSerializers.CHEESE_WEDGE.get();
    }
}
