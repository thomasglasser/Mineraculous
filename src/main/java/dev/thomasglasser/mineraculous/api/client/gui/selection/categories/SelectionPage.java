package dev.thomasglasser.mineraculous.api.client.gui.selection.categories;

import com.google.common.base.MoreObjects;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import java.util.List;

/**
 * Represents a single page of a {@link SelectionMenu}, which includes a list of
 * {@link SelectionMenuItem}s to display and the currently selected slot.
 */
public class SelectionPage {
    private final List<SelectionMenuItem> items;
    private final int selection;

    public SelectionPage(List<SelectionMenuItem> items, int selection) {
        this.items = items;
        this.selection = selection;
    }

    public SelectionMenuItem getItem(int index) {
        return index >= 0 && index < this.items.size()
                ? MoreObjects.firstNonNull(this.items.get(index), SelectionMenu.EMPTY_SLOT)
                : SelectionMenu.EMPTY_SLOT;
    }

    public int getSelectedSlot() {
        return this.selection;
    }
}
