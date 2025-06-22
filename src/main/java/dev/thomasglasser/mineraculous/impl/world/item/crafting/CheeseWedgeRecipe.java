package dev.thomasglasser.mineraculous.impl.world.item.crafting;

import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
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

public class CheeseWedgeRecipe extends CustomRecipe {
    public CheeseWedgeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> items = new ReferenceArrayList<>();
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        if (items.size() == 1) {
            ItemStack only = items.getFirst();
            if (only.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AgeingCheeseEdibleFullBlock cheeseBlock) {
                Integer bites = only.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(cheeseBlock.getMissingPiecesProperty());
                return bites == null || bites < cheeseBlock.getMaxMissingPieces();
            }
        } else if (items.size() > 1) {
            ItemStack first = items.getFirst();
            if (first.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AgeingCheeseEdibleFullBlock cheeseBlock) {
                for (ItemStack stack : items) {
                    if (stack.is(first.getItem())) {
                        Integer bites = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(cheeseBlock.getMissingPiecesProperty());
                        if (bites == null || bites != cheeseBlock.getMaxMissingPieces()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> items = new ReferenceArrayList<>();
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        if (items.size() == 1) {
            ItemStack only = items.getFirst();
            if (only.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AgeingCheeseEdibleFullBlock cheese) {
                Integer bites = only.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(cheese.getMissingPiecesProperty());
                if (bites == null)
                    bites = 0;
                return new ItemStack(cheese.getPiece(), 4 - bites);
            }
        } else if (items.size() > 1) {
            if (items.getFirst().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AgeingCheeseEdibleFullBlock cheese) {
                int bites = items.size();
                ItemStack result = cheese.asItem().getDefaultInstance();
                result.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(cheese.getMissingPiecesProperty(), 4 - bites));
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
