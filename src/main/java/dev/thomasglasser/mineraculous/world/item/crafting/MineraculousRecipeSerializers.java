package dev.thomasglasser.mineraculous.world.item.crafting;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MineraculousRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, CustomRecipe.Serializer<CheeseWedgeRecipe>> CHEESE_WEDGE = RECIPE_SERIALIZERS.register("cheese_wedge", () -> new CustomRecipe.Serializer<>(CheeseWedgeRecipe::new));

    public static void init() {}
}
