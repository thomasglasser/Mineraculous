package dev.thomasglasser.mineraculous.api.client.gui.selection;

import com.google.common.base.MoreObjects;
import dev.thomasglasser.mineraculous.api.client.gui.selection.categories.SelectionPage;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * A menu similar to the {@link SpectatorMenu} that allows going through and selecting {@link SpectatorMenuItem}s.
 */
public class SelectionMenu {
    protected static final SelectionMenuItem CLOSE_ITEM = new CloseSelectionItem();
    protected static final SelectionMenuItem SCROLL_LEFT = new ScrollMenuItem(-1, true);
    protected static final SelectionMenuItem SCROLL_RIGHT_ENABLED = new ScrollMenuItem(1, true);
    protected static final SelectionMenuItem SCROLL_RIGHT_DISABLED = new ScrollMenuItem(1, false);
    public static final SelectionMenuItem EMPTY_SLOT = new SelectionMenuItem() {
        @Override
        public void selectItem(SelectionMenu menu) {}

        @Override
        public Component getName() {
            return CommonComponents.EMPTY;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float alpha) {}

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
    protected final SelectionMenuListener listener;
    protected final SelectionMenuCategory category;
    protected int selectedSlot = SpectatorPage.NO_SELECTION;
    int page;

    public SelectionMenu(SelectionMenuListener listener, SelectionMenuCategory category) {
        this.listener = listener;
        this.category = category;
    }

    public SelectionMenuItem getItem(int index) {
        int i = index + this.page * 6;
        if (this.page > 0 && index == 0) {
            return SCROLL_LEFT;
        } else if (index == SpectatorMenu.MAX_PER_PAGE - 1) {
            return i < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED;
        } else if (index == SpectatorMenu.MAX_PER_PAGE) {
            return CLOSE_ITEM;
        } else {
            return i >= 0 && i < this.category.getItems().size() ? MoreObjects.firstNonNull(this.category.getItems().get(i), EMPTY_SLOT) : EMPTY_SLOT;
        }
    }

    public List<SelectionMenuItem> getItems() {
        List<SelectionMenuItem> list = new ReferenceArrayList<>();

        for (int i = 0; i <= 8; i++) {
            list.add(this.getItem(i));
        }

        return list;
    }

    public SelectionMenuItem getSelectedItem() {
        return this.getItem(this.selectedSlot);
    }

    public SelectionMenuCategory getSelectedCategory() {
        return this.category;
    }

    public void selectSlot(int slot) {
        SelectionMenuItem selectionMenuItem = this.getItem(slot);
        if (selectionMenuItem != EMPTY_SLOT) {
            if (this.selectedSlot == slot && selectionMenuItem.isEnabled()) {
                selectionMenuItem.selectItem(this);
            } else {
                this.selectedSlot = slot;
            }
        }
    }

    public void exit() {
        this.listener.onMenuClosed(this);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public SelectionPage getCurrentPage() {
        return new SelectionPage(this.getItems(), this.selectedSlot);
    }

    static class CloseSelectionItem implements SelectionMenuItem {
        @Override
        public void selectItem(SelectionMenu menu) {
            menu.exit();
        }

        @Override
        public Component getName() {
            return SpectatorMenu.CLOSE_MENU_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float alpha) {
            guiGraphics.blitSprite(SpectatorMenu.CLOSE_SPRITE, 0, 0, 16, 16);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ScrollMenuItem implements SelectionMenuItem {
        private final int direction;
        private final boolean enabled;

        public ScrollMenuItem(int direction, boolean enabled) {
            this.direction = direction;
            this.enabled = enabled;
        }

        @Override
        public void selectItem(SelectionMenu menu) {
            menu.page = menu.page + this.direction;
        }

        @Override
        public Component getName() {
            return this.direction < 0 ? SpectatorMenu.PREVIOUS_PAGE_TEXT : SpectatorMenu.NEXT_PAGE_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float alpha) {
            if (this.direction < 0) {
                guiGraphics.blitSprite(SpectatorMenu.SCROLL_LEFT_SPRITE, 0, 0, 16, 16);
            } else {
                guiGraphics.blitSprite(SpectatorMenu.SCROLL_RIGHT_SPRITE, 0, 0, 16, 16);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }
    }
}
