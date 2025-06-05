package dev.thomasglasser.mineraculous.client.gui.kamiko.categories;

import com.google.common.base.MoreObjects;
import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenu;
import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenuItem;
import java.util.List;

public class KamikoPage {
    private final List<KamikoMenuItem> items;
    private final int selection;

    public KamikoPage(List<KamikoMenuItem> items, int selection) {
        this.items = items;
        this.selection = selection;
    }

    public KamikoMenuItem getItem(int index) {
        return index >= 0 && index < this.items.size()
                ? MoreObjects.firstNonNull(this.items.get(index), KamikoMenu.EMPTY_SLOT)
                : KamikoMenu.EMPTY_SLOT;
    }

    public int getSelectedSlot() {
        return this.selection;
    }
}
