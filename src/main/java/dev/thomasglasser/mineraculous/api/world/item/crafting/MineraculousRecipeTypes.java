package dev.thomasglasser.mineraculous.api.world.item.crafting;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class MineraculousRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<OvenCookingRecipe>> OVEN_COOKING = register("oven_cooking");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String name) {
        ResourceLocation id = MineraculousConstants.modLoc(name);
        return RECIPE_TYPES.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    public static void init() {}
}
