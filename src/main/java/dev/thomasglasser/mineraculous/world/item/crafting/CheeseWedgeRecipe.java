package dev.thomasglasser.mineraculous.world.item.crafting;

import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CheeseWedgeRecipe extends CustomRecipe
{
	public CheeseWedgeRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	@Override
	public boolean matches(CraftingInput craftingInput, Level level)
	{
		if (!this.canCraftInDimensions(craftingInput.width(), craftingInput.height()))
		{
			return false;
		} else
		{
			List<ItemStack> items = craftingInput.items().stream().filter(stack -> !stack.isEmpty()).toList();
			if (items.size() != 1)
			{
				return false;
			}
			else
			{
				return items.getFirst().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed();
			}
		}
	}

	@Override
	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider)
	{
		ItemStack itemStack = craftingInput.items().stream().filter(stack -> !stack.isEmpty()).toList().getFirst();
		BlockItemStateProperties properties = itemStack.get(DataComponents.BLOCK_STATE);
		if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed())
		{
			Integer bites = null;
			if (properties != null)
				bites = properties.get(CheeseBlock.BITES);
			if (bites == null) bites = 0;
			return new ItemStack(cheeseBlock.getWedge(cheeseBlock.getAge()).get(), 4 - bites);
		}
		return ItemStack.EMPTY;	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width * height >= 1;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return MineraculousRecipeSerializers.CHEESE_WEDGE.get();
	}
}
