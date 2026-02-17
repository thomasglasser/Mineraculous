package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ToolModeItem implements SelectionMenuItem {
    protected final Component name;
    protected final ItemStack toolStack;

    public ToolModeItem(ItemStack stack) {
        name = stack.getDisplayName();
        toolStack = stack.copy();
    }

    public ItemStack getToolStack() {
        return toolStack;
    }

    @Override
    public void selectItem(SelectionMenu menu) {}

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float alpha) {
        guiGraphics.renderItem(toolStack, 2, 2);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
