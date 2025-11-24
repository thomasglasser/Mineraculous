package dev.thomasglasser.mineraculous.impl.plugins.jei;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.inventory.MineraculousMenuTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.crafting.MineraculousRecipeTypes;
import dev.thomasglasser.mineraculous.api.world.item.crafting.OvenCookingRecipe;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.world.inventory.OvenMenu;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.function.Supplier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.library.plugins.vanilla.cooking.AbstractCookingCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class MineraculousJeiPlugin implements IModPlugin {
    private static final Supplier<RecipeType<RecipeHolder<OvenCookingRecipe>>> OVEN_COOKING_RECIPE_TYPE = RecipeType.createFromDeferredVanilla(MineraculousRecipeTypes.OVEN_COOKING);
    public static final String OVEN_COOKING_CATEGORY = "gui.jei.category.mineraculous.oven_cooking";

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, MineraculousItems.MIRACULOUS.get(), MiraculousSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, MineraculousItems.KWAMI.get(), MiraculousSubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AbstractCookingCategory<>(registration.getJeiHelpers().getGuiHelper(), OVEN_COOKING_RECIPE_TYPE.get(), MineraculousBlocks.OVEN.get(), OVEN_COOKING_CATEGORY, OvenBlockEntity.BURN_TIME_STANDARD) {});
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(OVEN_COOKING_RECIPE_TYPE.get(), ClientUtils.getLevel().getRecipeManager().getAllRecipesFor(MineraculousRecipeTypes.OVEN_COOKING.get()));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(OvenMenu.class, MineraculousMenuTypes.OVEN.get(), OVEN_COOKING_RECIPE_TYPE.get(), OvenMenu.INGREDIENT_SLOT, OvenMenu.INGREDIENT_SLOTS.length, OvenMenu.INV_SLOT_START, OvenMenu.USE_ROW_SLOT_END - OvenMenu.INV_SLOT_START);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MineraculousBlocks.OVEN.toStack(), OVEN_COOKING_RECIPE_TYPE.get());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return MineraculousConstants.modLoc(MineraculousConstants.MOD_ID);
    }
}
