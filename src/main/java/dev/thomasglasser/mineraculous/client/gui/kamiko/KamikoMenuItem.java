package dev.thomasglasser.mineraculous.client.gui.kamiko;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface KamikoMenuItem {
    void selectItem(KamikoMenu menu);

    Component getName();

    void renderIcon(GuiGraphics p_282591_, float p_101840_, float p_361027_);

    boolean isEnabled();
}
