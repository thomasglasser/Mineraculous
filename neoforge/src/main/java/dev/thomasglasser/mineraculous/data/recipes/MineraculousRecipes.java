package dev.thomasglasser.mineraculous.data.recipes;

import dev.thomasglasser.mineraculous.world.item.crafting.CheeseWedgeRecipe;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.tommylib.api.data.recipes.ExtendedRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;

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

		SpecialRecipeBuilder.special(CheeseWedgeRecipe::new).save(pRecipeOutput, "cheese_wedge");
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
}