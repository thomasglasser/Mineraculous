package dev.thomasglasser.mineraculous.world.item.crafting;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class MineraculousRecipeSerializers
{
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mineraculous.MOD_ID);

	public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<CheeseWedgeRecipe>> CHEESE_WEDGE = RECIPE_SERIALIZERS.register("cheese_wedge", () -> new SimpleCraftingRecipeSerializer<>(CheeseWedgeRecipe::new));

	public static void init() {}
}
