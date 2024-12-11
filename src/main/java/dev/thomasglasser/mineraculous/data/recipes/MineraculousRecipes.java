package dev.thomasglasser.mineraculous.data.recipes;

import dev.thomasglasser.mineraculous.world.item.crafting.CheeseWedgeRecipe;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.recipes.ExtendedRecipeProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;

public class MineraculousRecipes extends ExtendedRecipeProvider {
    public MineraculousRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider holderLookup) {
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CHEESE_BLOCKS);
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CAMEMBERT_BLOCKS);

        SpecialRecipeBuilder.special(CheeseWedgeRecipe::new).save(recipeOutput, "cheese_wedge");
    }

    protected void cheeseWaxRecipes(RecipeOutput recipeOutput, SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxables) {
        waxables
                .forEach(
                        (age, block) -> {
                            DeferredBlock<CheeseBlock> waxed = block.get().getWaxedBlock();
                            if (waxed.get().requiredFeatures().isSubsetOf(FeatureFlags.DEFAULT_FLAGS)) {
                                ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, waxed)
                                        .requires(block)
                                        .requires(Items.HONEYCOMB)
                                        .group(getItemName(waxed))
                                        .unlockedBy(getHasName(block), has(block))
                                        .save(recipeOutput, getConversionRecipeName(waxed, Items.HONEYCOMB));
                            }
                        });
    }
}
