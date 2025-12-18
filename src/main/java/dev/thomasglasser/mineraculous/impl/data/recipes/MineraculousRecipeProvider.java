package dev.thomasglasser.mineraculous.impl.data.recipes;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.crafting.OvenCookingRecipe;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.SimpleTransmuteCookingRecipeBuilder;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.SimpleTransmuteCookingSerializer;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteCampfireCookingRecipe;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteOvenCookingRecipe;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteSmokingRecipe;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.CheeseWedgeRecipe;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import dev.thomasglasser.tommylib.api.data.recipes.ExtendedRecipeProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class MineraculousRecipeProvider extends ExtendedRecipeProvider {
    public MineraculousRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider holderLookup) {
        buildCrafting(recipeOutput);
        buildCooking(recipeOutput);
    }

    private void buildCrafting(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MineraculousBlocks.CHEESE_POT)
                .pattern("I I")
                .pattern("ICI")
                .pattern("III")
                .define('I', ConventionalItemTags.IRON_INGOTS)
                .define('C', MineraculousItemTags.CHEESE_BLOCKS)
                .unlockedBy(getHasName(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                .unlockedBy(getHasName(MineraculousItemTags.CHEESE_BLOCKS), has(MineraculousItemTags.CHEESE_BLOCKS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MineraculousBlocks.OVEN)
                .pattern("III")
                .pattern("IFI")
                .pattern("PPP")
                .define('I', ConventionalItemTags.IRON_INGOTS)
                .define('F', Items.FURNACE)
                .define('P', ItemTags.PLANKS)
                .unlockedBy(getHasName(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                .unlockedBy(getHasName(Items.FURNACE), has(Items.FURNACE))
                .unlockedBy(getHasName(ItemTags.PLANKS), has(ItemTags.PLANKS))
                .save(recipeOutput);

        // Cheese
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CHEESE, MineraculousBlocks.WAXED_CHEESE);
        cheeseWaxRecipes(recipeOutput, MineraculousBlocks.CAMEMBERT, MineraculousBlocks.WAXED_CAMEMBERT);

        SpecialRecipeBuilder.special(CheeseWedgeRecipe::new).save(recipeOutput, MineraculousConstants.modLoc("cheese_wedge"));

        trimWithCopy(recipeOutput, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.RED_CONCRETE);
        trimWithCopy(recipeOutput, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.LIME_CONCRETE);
        trimWithCopy(recipeOutput, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE, Blocks.PURPLE_CONCRETE);
    }

    private void buildCooking(RecipeOutput recipeOutput) {
        cookRecipes(recipeOutput, "oven_cooking", MineraculousRecipeSerializers.OVEN_COOKING.get(), OvenCookingRecipe::new, 400);

        simpleTransmuteCookingRecipes(recipeOutput, MineraculousItems.RAW_MACARON, MineraculousItems.MACARON, 0.35f);
    }

    public static void simpleCookingRecipes(RecipeOutput recipeOutput, ItemLike input, ItemLike output, float experience) {
        ExtendedRecipeProvider.simpleCookingRecipes(recipeOutput, input, output, experience);
        simpleOvenCookingRecipe(recipeOutput, input, output, experience);
    }

    public static void simpleOvenCookingRecipe(RecipeOutput recipeOutput, ItemLike ingredient, ItemLike result, float experience) {
        simpleCookingRecipe(recipeOutput, "oven_cooking", MineraculousRecipeSerializers.OVEN_COOKING.get(), OvenCookingRecipe::new, OvenBlockEntity.BURN_TIME_STANDARD, ingredient, result, experience);
    }

    public static void simpleTransmuteSmeltingRecipe(RecipeOutput recipeOutput, ItemLike ingredient, ItemLike result, float experience) {
        SimpleTransmuteCookingRecipeBuilder.smelting(Ingredient.of(ingredient), RecipeCategory.FOOD, result, experience, 200)
                .unlockedBy(getHasName(ingredient), has(ingredient))
                .save(recipeOutput);
    }

    public static <T extends AbstractCookingRecipe> void simpleTransmuteCookingRecipe(
            RecipeOutput recipeOutput,
            String cookingMethod,
            RecipeSerializer<T> cookingSerializer,
            SimpleTransmuteCookingSerializer.Factory<T> recipeFactory,
            int cookingTime,
            ItemLike material,
            ItemLike result,
            float experience) {
        SimpleTransmuteCookingRecipeBuilder.generic(Ingredient.of(material), RecipeCategory.FOOD, result, experience, cookingTime, cookingSerializer, recipeFactory)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, BuiltInRegistries.ITEM.getKey(result.asItem()).withSuffix("_from_" + cookingMethod));
    }

    public static void simpleTransmuteSmokingRecipe(RecipeOutput recipeOutput, ItemLike ingredient, ItemLike result, float experience) {
        simpleTransmuteCookingRecipe(recipeOutput, "smoking", RecipeSerializer.SMOKING_RECIPE, TransmuteSmokingRecipe::new, 100, ingredient, result, experience);
    }

    public static void simpleTransmuteCampfireCookingRecipe(RecipeOutput recipeOutput, ItemLike ingredient, ItemLike result, float experience) {
        simpleTransmuteCookingRecipe(recipeOutput, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, TransmuteCampfireCookingRecipe::new, 600, ingredient, result, experience);
    }

    public static void simpleTransmuteOvenCookingRecipe(RecipeOutput recipeOutput, ItemLike ingredient, ItemLike result, float experience) {
        simpleTransmuteCookingRecipe(recipeOutput, "oven_cooking", MineraculousRecipeSerializers.OVEN_COOKING.get(), TransmuteOvenCookingRecipe::new, 400, ingredient, result, experience);
    }

    public static void simpleTransmuteCookingRecipes(RecipeOutput recipeOutput, ItemLike input, ItemLike output, float experience) {
        simpleTransmuteSmeltingRecipe(recipeOutput, input, output, experience);
        simpleTransmuteSmokingRecipe(recipeOutput, input, output, experience);
        simpleTransmuteCampfireCookingRecipe(recipeOutput, input, output, experience);
        simpleTransmuteOvenCookingRecipe(recipeOutput, input, output, experience);
    }

    protected static void cheeseWaxRecipes(RecipeOutput recipeOutput, Map<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> waxables, Map<AgeingCheese.Age, DeferredBlock<PieceBlock>> waxedBlocks) {
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
}
