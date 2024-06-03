package dev.thomasglasser.mineraculous.data.recipes;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.recipes.ExtendedRecipeProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

public class MineraculousRecipes extends ExtendedRecipeProvider
{

	public MineraculousRecipes(PackOutput p_248933_, CompletableFuture<HolderLookup.Provider> lookupProvider)
	{
		super(p_248933_, lookupProvider);
	}

	@Override
	protected void buildRecipes(RecipeOutput pRecipeOutput)
	{
		cheeseWaxRecipes(pRecipeOutput, FeatureFlags.DEFAULT_FLAGS);

		cheeseWedgeRecipes(pRecipeOutput, MineraculousBlocks.CHEESE_BLOCKS, MineraculousItems.CHEESE_WEDGES);
		cheeseWedgeRecipes(pRecipeOutput, MineraculousBlocks.CAMEMBERT_BLOCKS, MineraculousItems.CAMEMBERT_WEDGES);
	}

	protected static void cheeseWaxRecipes(RecipeOutput pRecipeOutput, FeatureFlagSet pRequiredFeatures) {
		CheeseBlock.WAXABLES
				.get()
				.forEach(
						(block, waxed) -> {
							if (waxed.requiredFeatures().isSubsetOf(pRequiredFeatures)) {
								ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, waxed)
										.requires(block)
										.requires(Items.HONEYCOMB)
										.group(getItemName(waxed))
										.unlockedBy(getHasName(block), has(block))
										.save(pRecipeOutput, getConversionRecipeName(waxed, Items.HONEYCOMB));
							}
						}
				);
	}

	protected static void cheeseWedgeRecipes(RecipeOutput recipeOutput, SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, SortedMap<CheeseBlock.Age, DeferredItem<Item>> wedges)
	{
		blocks.forEach((age, block) -> {
			ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, wedges.get(age), 4)
					.requires(block)
					.group(getItemName(wedges.get(age)))
					.unlockedBy(getHasName(block), has(block))
					.save(recipeOutput, getConversionRecipeName(wedges.get(age), block));
		});
	}
}
