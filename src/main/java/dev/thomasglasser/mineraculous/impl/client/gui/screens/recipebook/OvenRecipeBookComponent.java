package dev.thomasglasser.mineraculous.impl.client.gui.screens.recipebook;

import dev.thomasglasser.mineraculous.impl.world.inventory.OvenMenu;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class OvenRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    public static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.oven_cookable");

    @Override
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }

    @Override
    public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
        ItemStack resultItem = recipe.value().getResultItem(this.minecraft.level.registryAccess());
        resultItem = resultItem.copyWithCount(resultItem.getCount() * 4);
        this.ghostRecipe.setRecipe(recipe);
        this.ghostRecipe.addIngredient(Ingredient.of(resultItem), slots.get(OvenMenu.RESULT_SLOT).x, slots.get(OvenMenu.RESULT_SLOT).y);
        Slot fuelSlot = slots.get(OvenMenu.FUEL_SLOT);
        if (fuelSlot.getItem().isEmpty()) {
            if (this.fuels == null) {
                this.fuels = Ingredient.of(
                        this.getFuelItems().stream().filter(item -> item.isEnabled(this.minecraft.level.enabledFeatures())).map(ItemStack::new));
            }

            this.ghostRecipe.addIngredient(this.fuels, fuelSlot.x, fuelSlot.y);
        }
        NonNullList<Ingredient> ingredients = recipe.value().getIngredients();
        Iterator<Ingredient> iterator = ingredients.iterator();
        if (!iterator.hasNext()) {
            return;
        }

        Ingredient ingredient = iterator.next();
        if (!ingredient.isEmpty()) {
            for (int i : OvenMenu.INGREDIENT_SLOTS) {
                Slot slot = slots.get(i);
                this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
            }
        }
    }
}
