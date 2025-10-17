package dev.thomasglasser.mineraculous.impl.world.item.crafting;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.SimpleTransmuteCookingSerializer;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteCampfireCookingRecipe;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteSmeltingRecipe;
import dev.thomasglasser.mineraculous.api.world.item.crafting.transmute.TransmuteSmokingRecipe;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<CheeseWedgeRecipe>> CHEESE_WEDGE = RECIPE_SERIALIZERS.register("crafting_special_cheesewedge", () -> new SimpleCraftingRecipeSerializer<>(CheeseWedgeRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleTransmuteCookingSerializer<TransmuteSmeltingRecipe>> SMELTING_TRANSMUTE = RECIPE_SERIALIZERS.register("smelting_transmute", () -> new SimpleTransmuteCookingSerializer<>(TransmuteSmeltingRecipe::new, 200));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleTransmuteCookingSerializer<TransmuteSmokingRecipe>> SMOKING_TRANSMUTE = RECIPE_SERIALIZERS.register("smoking_transmute", () -> new SimpleTransmuteCookingSerializer<>(TransmuteSmokingRecipe::new, 100));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleTransmuteCookingSerializer<TransmuteCampfireCookingRecipe>> CAMPFIRE_COOKING_TRANSMUTE = RECIPE_SERIALIZERS.register("campfire_cooking_transmute", () -> new SimpleTransmuteCookingSerializer<>(TransmuteCampfireCookingRecipe::new, 100));

    @ApiStatus.Internal
    public static void init() {}
}
