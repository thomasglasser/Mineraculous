package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ToolModeItem implements SelectionMenuItem {
    protected final Component name;
    protected final ItemStack itemStack;

    public ToolModeItem(ItemStack stack) {
        name = stack.getDisplayName();
        itemStack = stack.copy();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void selectItem(SelectionMenu menu) {}

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float alpha) {
        guiGraphics.renderItem(itemStack, 2, 2);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
