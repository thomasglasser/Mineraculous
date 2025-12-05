package dev.thomasglasser.mineraculous.api.client.gui.selection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/// Can be shown and selected in a {@link SelectionMenu} to trigger an action.
public interface SelectionMenuItem {
    void selectItem(SelectionMenu menu);

    Component getName();

    void renderIcon(GuiGraphics guiGraphics, float alpha);

    boolean isEnabled();
}
