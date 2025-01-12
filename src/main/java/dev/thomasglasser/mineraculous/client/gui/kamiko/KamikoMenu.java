package dev.thomasglasser.mineraculous.client.gui.kamiko;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import dev.thomasglasser.mineraculous.client.gui.kamiko.categories.KamikoPage;
import dev.thomasglasser.mineraculous.client.gui.kamiko.categories.TargetPlayerMenuCategory;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class KamikoMenu {
    static final ResourceLocation CLOSE_SPRITE = ResourceLocation.withDefaultNamespace("spectator/close");
    static final ResourceLocation SCROLL_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("spectator/scroll_left");
    static final ResourceLocation SCROLL_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("spectator/scroll_right");
    private static final KamikoMenuItem CLOSE_ITEM = new CloseKamikoItem();
    private static final KamikoMenuItem SCROLL_LEFT = new ScrollMenuItem(-1, true);
    private static final KamikoMenuItem SCROLL_RIGHT_ENABLED = new ScrollMenuItem(1, true);
    private static final KamikoMenuItem SCROLL_RIGHT_DISABLED = new ScrollMenuItem(1, false);
    private static final int MAX_PER_PAGE = 8;
    static final Component CLOSE_MENU_TEXT = Component.translatable("spectatorMenu.close");
    static final Component PREVIOUS_PAGE_TEXT = Component.translatable("spectatorMenu.previous_page");
    static final Component NEXT_PAGE_TEXT = Component.translatable("spectatorMenu.next_page");
    public static final KamikoMenuItem EMPTY_SLOT = new KamikoMenuItem() {
        @Override
        public void selectItem(KamikoMenu p_101812_) {}

        @Override
        public Component getName() {
            return CommonComponents.EMPTY;
        }

        @Override
        public void renderIcon(GuiGraphics p_283652_, float p_101809_, float p_363818_) {}

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
    private final KamikoMenuListener listener;
    private KamikoMenuCategory category;
    private int selectedSlot = KamikoPage.NO_SELECTION;
    int page;

    public KamikoMenu(KamikoMenuListener listener) {
        this.category = new TargetPlayerMenuCategory();
        this.listener = listener;
    }

    public KamikoMenuItem getItem(int index) {
        int i = index + this.page * 6;
        if (this.page > 0 && index == 0) {
            return SCROLL_LEFT;
        } else if (index == MAX_PER_PAGE - 1) {
            return i < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED;
        } else if (index == MAX_PER_PAGE) {
            return CLOSE_ITEM;
        } else {
            return i >= 0 && i < this.category.getItems().size() ? MoreObjects.firstNonNull(this.category.getItems().get(i), EMPTY_SLOT) : EMPTY_SLOT;
        }
    }

    public List<KamikoMenuItem> getItems() {
        List<KamikoMenuItem> list = Lists.newArrayList();

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
        KamikoMenuItem kamikomenuitem = this.getItem(slot);
        if (kamikomenuitem != EMPTY_SLOT) {
            if (this.selectedSlot == slot && kamikomenuitem.isEnabled()) {
                kamikomenuitem.selectItem(this);
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

    public void selectCategory(KamikoMenuCategory category) {
        this.category = category;
        this.selectedSlot = KamikoPage.NO_SELECTION;
        this.page = 0;
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
            return KamikoMenu.CLOSE_MENU_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics p_283113_, float p_282295_, float p_362741_) {
            p_283113_.blitSprite(KamikoMenu.CLOSE_SPRITE, 0, 0, 16, 16);
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
            return this.direction < 0 ? KamikoMenu.PREVIOUS_PAGE_TEXT : KamikoMenu.NEXT_PAGE_TEXT;
        }

        @Override
        public void renderIcon(GuiGraphics p_281376_, float p_282065_, float p_363582_) {
            if (this.direction < 0) {
                p_281376_.blitSprite(KamikoMenu.SCROLL_LEFT_SPRITE, 0, 0, 16, 16);
            } else {
                p_281376_.blitSprite(KamikoMenu.SCROLL_RIGHT_SPRITE, 0, 0, 16, 16);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }
    }
}
