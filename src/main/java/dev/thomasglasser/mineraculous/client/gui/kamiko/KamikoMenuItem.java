package dev.thomasglasser.mineraculous.client.gui.kamiko;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface KamikoMenuItem {
    void selectItem(KamikoMenu menu);

    Component getName();

    void renderIcon(GuiGraphics guiGraphics, float alpha);

    boolean isEnabled();
}
