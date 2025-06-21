package dev.thomasglasser.mineraculous.impl.world.item.crafting;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<CheeseWedgeRecipe>> CHEESE_WEDGE = RECIPE_SERIALIZERS.register("crafting_special_cheesewedge", () -> new SimpleCraftingRecipeSerializer<>(CheeseWedgeRecipe::new));

    @ApiStatus.Internal
    public static void init() {}
}
