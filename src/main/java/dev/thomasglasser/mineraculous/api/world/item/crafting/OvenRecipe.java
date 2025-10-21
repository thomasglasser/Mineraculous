package dev.thomasglasser.mineraculous.api.world.item.crafting;

import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class OvenRecipe extends AbstractCookingRecipe {
    public OvenRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(MineraculousRecipeTypes.OVEN.get(), group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack getToastSymbol() {
        return MineraculousBlocks.OVEN.toStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MineraculousRecipeSerializers.OVEN.get();
    }
}
