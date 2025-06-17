package dev.thomasglasser.mineraculous.api.client.gui.selection;

import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Can be selected in a {@link SelectionMenu} to show a list of {@link SelectionMenuItem}s.
 */
public interface SelectionMenuCategory {
    List<SelectionMenuItem> getItems();

    Component getPrompt();
}
