package dev.thomasglasser.mineraculous.impl.world.inventory;

import dev.thomasglasser.mineraculous.api.world.inventory.MineraculousMenuTypes;
import dev.thomasglasser.mineraculous.api.world.item.crafting.MineraculousRecipeTypes;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import java.util.Arrays;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class OvenMenu extends AbstractFurnaceMenu {
    public static final int[] INGREDIENT_SLOTS = { 0, 1, 2, 3 };
    public static final int FUEL_SLOT = 4;
    public static final int RESULT_SLOT = 5;
    public static final int SLOT_COUNT = 6;
    public static final int DATA_COUNT = 4;
    public static final int INV_SLOT_START = 6;
    public static final int INV_SLOT_END = 33;
    public static final int USE_ROW_SLOT_START = 33;
    public static final int USE_ROW_SLOT_END = 42;

    public OvenMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public OvenMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(MineraculousMenuTypes.OVEN.get(), MineraculousRecipeTypes.OVEN.get(), MineraculousRecipeBookTypes.OVEN.getValue(), containerId, playerInventory, container, data);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.slots.clear();
        this.lastSlots.clear();
        this.remoteSlots.clear();
        for (int i = 0; i < INGREDIENT_SLOTS.length; i++) {
            this.addSlot(new OvenIngredientSlot(this, container, INGREDIENT_SLOTS[i], 25 + (i * 22), 16));
        }
        this.addSlot(new FurnaceFuelSlot(this, container, FUEL_SLOT, 58, 55));
        this.addSlot(new FurnaceResultSlot(playerInventory.player, container, RESULT_SLOT, 139, 47));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int i : INGREDIENT_SLOTS) {
            this.getSlot(i).set(ItemStack.EMPTY);
        }
        this.getSlot(RESULT_SLOT).set(ItemStack.EMPTY);
    }

    @Override
    public boolean recipeMatches(RecipeHolder<AbstractCookingRecipe> recipe) {
        ItemStack input = OvenBlockEntity.findFirstInput(this.getItems());
        return recipe.value().matches(new SingleRecipeInput(input), this.level);
    }

    @Override
    public int getResultSlotIndex() {
        return RESULT_SLOT;
    }

    @Override
    public int getGridWidth() {
        return 4;
    }

    @Override
    public int getSize() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(slotItem, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotItem, result);
            } else if (index != FUEL_SLOT && Arrays.stream(INGREDIENT_SLOTS).noneMatch(i -> i == index)) {
                if (this.canSmelt(slotItem)) {
                    if (!this.moveItemStackTo(slotItem, INGREDIENT_SLOT, INGREDIENT_SLOTS.length, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(slotItem)) {
                    if (!this.moveItemStackTo(slotItem, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
                    if (!this.moveItemStackTo(slotItem, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && !this.moveItemStackTo(slotItem, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotItem.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotItem);
        }

        return result;
    }

    static class OvenIngredientSlot extends Slot {
        protected final OvenMenu menu;

        public OvenIngredientSlot(OvenMenu menu, Container container, int index, int xPosition, int yPosition) {
            super(container, index, xPosition, yPosition);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ItemStack input = OvenBlockEntity.findFirstInput(menu.getItems());
            return input.isEmpty() || ItemStack.isSameItemSameComponents(stack, input);
        }
    }
}
