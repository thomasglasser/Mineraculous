package dev.thomasglasser.mineraculous.impl.data.recipes;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.CheeseWedgeRecipe;
import dev.thomasglasser.tommylib.api.data.recipes.ExtendedRecipeProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class MineraculousRecipeProvider extends ExtendedRecipeProvider {
    public MineraculousRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider holderLookup) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MineraculousBlocks.CHEESE_POT.get())
                .pattern("I I")
                .pattern("ICI")
                .pattern("III")
                .define('I', ConventionalItemTags.IRON_INGOTS)
                .define('C', MineraculousItemTags.CHEESE_BLOCKS)
                .unlockedBy("has_cheese_blocks", has(MineraculousItemTags.CHEESE_BLOCKS))
                .save(recipeOutput);

        // Cheese
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CHEESE, MineraculousBlocks.WAXED_CHEESE);
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CAMEMBERT, MineraculousBlocks.WAXED_CAMEMBERT);

        SpecialRecipeBuilder.special(CheeseWedgeRecipe::new).save(recipeOutput, MineraculousConstants.modLoc("cheese_wedge"));

        trimWithCopy(recipeOutput, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.RED_CONCRETE);
        trimWithCopy(recipeOutput, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.LIME_CONCRETE);
        trimWithCopy(recipeOutput, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.PURPLE_CONCRETE);
    }

    protected void cheeseWaxRecipes(RecipeOutput recipeOutput, SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> waxables, SortedMap<AgeingCheese.Age, DeferredBlock<PieceBlock>> waxedBlocks) {
        waxables.forEach((age, block) -> {
            DeferredBlock<PieceBlock> waxed = waxedBlocks.get(age);
            if (waxed.get().requiredFeatures().isSubsetOf(FeatureFlags.DEFAULT_FLAGS)) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, waxed)
                        .requires(block)
                        .requires(Items.HONEYCOMB)
                        .group(getItemName(waxed))
                        .unlockedBy(getHasName(block), has(block))
                        .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(waxed.getId().getNamespace(), getConversionRecipeName(waxed, Items.HONEYCOMB)));
            }
        });
    }

    private void trimWithCopy(RecipeOutput recipeOutput, ItemLike template, ItemLike copyMaterial) {
        trimSmithing(recipeOutput, template.asItem(), MineraculousConstants.modLoc(getItemName(template) + "_smithing_trim"));
        copySmithingTemplate(recipeOutput, template, copyMaterial);
    }
}
