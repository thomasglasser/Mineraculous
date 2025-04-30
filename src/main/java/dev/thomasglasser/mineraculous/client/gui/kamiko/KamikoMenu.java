package dev.thomasglasser.mineraculous.client.gui.kamiko;

import com.google.common.base.MoreObjects;
import dev.thomasglasser.mineraculous.client.gui.kamiko.categories.KamikoPage;
import dev.thomasglasser.mineraculous.client.gui.kamiko.categories.TargetPlayerMenuCategory;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class KamikoMenu {
    private static final KamikoMenuItem CLOSE_ITEM = new CloseKamikoItem();
    private static final KamikoMenuItem SCROLL_LEFT = new ScrollMenuItem(-1, true);
    private static final KamikoMenuItem SCROLL_RIGHT_ENABLED = new ScrollMenuItem(1, true);
    private static final KamikoMenuItem SCROLL_RIGHT_DISABLED = new ScrollMenuItem(1, false);
    public static final KamikoMenuItem EMPTY_SLOT = new KamikoMenuItem() {
        @Override
        public void selectItem(KamikoMenu menu) {}

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
    private final KamikoMenuListener listener;
    private final KamikoMenuCategory category;
    private int selectedSlot = SpectatorPage.NO_SELECTION;
    int page;

    public KamikoMenu(KamikoMenuListener listener) {
        this.category = new TargetPlayerMenuCategory();
        this.listener = listener;
    }

    public KamikoMenuItem getItem(int index) {
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

    public List<KamikoMenuItem> getItems() {
        List<KamikoMenuItem> list = new ReferenceArrayList<>();

        for (int i = 0; i <= 8; i++) {
            list.add(this.getItem(i));
        }

        return list;
    }

    public KamikoMenuItem getSelectedItem() {
        return this.getItem(this.selectedSlot);
    }

    public KamikoMenuCategory getSelectedCategory() {
        return this.category;
    }

    public void selectSlot(int slot) {
        KamikoMenuItem kamikoMenuItem = this.getItem(slot);
        if (kamikoMenuItem != KamikoMenu.CLOSE_ITEM && Minecraft.getInstance().level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).size() == 0) {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable(Kamikotization.NO_KAMIKOTIZATIONS), true);
        } else if (kamikoMenuItem != EMPTY_SLOT) {
            if (this.selectedSlot == slot && kamikoMenuItem.isEnabled()) {
                kamikoMenuItem.selectItem(this);
            } else {
                this.selectedSlot = slot;
            }
        }
    }

    public void exit() {
        this.listener.onKamikoMenuClosed(this);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public KamikoPage getCurrentPage() {
        return new KamikoPage(this.getItems(), this.selectedSlot);
    }

    static class CloseKamikoItem implements KamikoMenuItem {
        @Override
        public void selectItem(KamikoMenu menu) {
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
    static class ScrollMenuItem implements KamikoMenuItem {
        private final int direction;
        private final boolean enabled;

        public ScrollMenuItem(int direction, boolean enabled) {
            this.direction = direction;
            this.enabled = enabled;
        }

        @Override
        public void selectItem(KamikoMenu menu) {
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
