package dev.thomasglasser.mineraculous.api.world.item.crafting.transmute;

import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;

public class TransmuteCampfireCookingRecipe extends CampfireCookingRecipe {
    private final ItemLike result;

    public TransmuteCampfireCookingRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemLike result, float experience, int cookingTime) {
        super(group, category, ingredient, result.asItem().getDefaultInstance(), experience, cookingTime);
        this.result = result;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return input.item().transmuteCopy(result, 1);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MineraculousRecipeSerializers.CAMPFIRE_COOKING_TRANSMUTE.get();
    }
}
