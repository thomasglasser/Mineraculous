package dev.thomasglasser.mineraculous.world.item.crafting;

import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
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
        List<ItemStack> items = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
        if (items.size() != 1) {
            return false;
        } else {
            return items.getFirst().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed();
        }
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack itemStack = input.items().stream().filter(stack -> !stack.isEmpty()).toList().getFirst();
        BlockItemStateProperties properties = itemStack.get(DataComponents.BLOCK_STATE);
        if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed()) {
            Integer bites = null;
            if (properties != null)
                bites = properties.get(CheeseBlock.BITES);
            if (bites == null) bites = 0;
            return new ItemStack(cheeseBlock.getWedge().get(), 4 - bites);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return MineraculousRecipeSerializers.CHEESE_WEDGE.get();
    }
}
