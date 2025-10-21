package dev.thomasglasser.mineraculous.api.world.item.crafting.transmute;

import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class SimpleTransmuteCookingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final ItemLike result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final SimpleTransmuteCookingSerializer.Factory<?> factory;

    private SimpleTransmuteCookingRecipeBuilder(
            RecipeCategory category,
            CookingBookCategory bookCategory,
            ItemLike result,
            Ingredient ingredient,
            float experience,
            int cookingTime,
            SimpleTransmuteCookingSerializer.Factory<?> factory) {
        this.category = category;
        this.bookCategory = bookCategory;
        this.result = result;
        this.ingredient = ingredient;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.factory = factory;
    }

    public static <T extends AbstractCookingRecipe> SimpleTransmuteCookingRecipeBuilder generic(
            Ingredient ingredient,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            RecipeSerializer<T> cookingSerializer,
            SimpleTransmuteCookingSerializer.Factory<T> factory) {
        return new SimpleTransmuteCookingRecipeBuilder(category, determineRecipeCategory(cookingSerializer, result), result, ingredient, experience, cookingTime, factory);
    }

    public static SimpleTransmuteCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int cookingTime) {
        return new SimpleTransmuteCookingRecipeBuilder(category, CookingBookCategory.FOOD, result, ingredient, experience, cookingTime, TransmuteCampfireCookingRecipe::new);
    }

    public static SimpleTransmuteCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int cookingTime) {
        return new SimpleTransmuteCookingRecipeBuilder(
                category, determineSmeltingRecipeCategory(result), result, ingredient, experience, cookingTime, TransmuteSmeltingRecipe::new);
    }

    public static SimpleTransmuteCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, ItemLike result, float experience, int cookingTime) {
        return new SimpleTransmuteCookingRecipeBuilder(category, CookingBookCategory.FOOD, result, ingredient, experience, cookingTime, TransmuteSmeltingRecipe::new);
    }

    public SimpleTransmuteCookingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public SimpleTransmuteCookingRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.asItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        AbstractCookingRecipe abstractcookingrecipe = this.factory
                .create(
                        Objects.requireNonNullElse(this.group, ""), this.bookCategory, this.ingredient, this.result, this.experience, this.cookingTime);
        recipeOutput.accept(id, abstractcookingrecipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike result) {
        if (result.asItem().components().has(DataComponents.FOOD)) {
            return CookingBookCategory.FOOD;
        } else {
            return result.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
        }
    }

    private static CookingBookCategory determineBlastingRecipeCategory(ItemLike result) {
        return result.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> serializer, ItemLike result) {
        if (serializer == RecipeSerializer.SMELTING_RECIPE) {
            return determineSmeltingRecipeCategory(result);
        } else if (serializer == RecipeSerializer.BLASTING_RECIPE) {
            return determineBlastingRecipeCategory(result);
        } else if (serializer != RecipeSerializer.SMOKING_RECIPE && serializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE && serializer != MineraculousRecipeSerializers.OVEN.get()) {
            throw new IllegalStateException("Unknown cooking recipe type");
        } else {
            return CookingBookCategory.FOOD;
        }
    }

    /**
     * Makes sure that this obtainable.
     */
    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
