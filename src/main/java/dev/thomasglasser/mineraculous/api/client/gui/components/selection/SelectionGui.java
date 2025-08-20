package dev.thomasglasser.mineraculous.api.client.gui.components.selection;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuCategory;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuListener;
import dev.thomasglasser.mineraculous.api.client.gui.selection.categories.SelectionPage;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * An HUD similar to the {@link SpectatorGui} for viewing and selecting a {@link SelectionMenuCategory} and {@link SelectionMenuItem}.
 * Handled by the provided {@link SelectionMenu}.
 */
public class SelectionGui implements SelectionMenuListener {
    private final Minecraft minecraft;
    private final Function<SelectionGui, SelectionMenu> menuFunction;
    private long lastSelectionTime;
    @Nullable
    private SelectionMenu menu;

    public SelectionGui(Minecraft minecraft, Function<SelectionGui, SelectionMenu> menuFunction) {
        this.minecraft = minecraft;
        this.menuFunction = menuFunction;
    }

    public void onHotbarSelected(int slot) {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu != null) {
            this.menu.selectSlot(slot);
        } else {
            this.menu = menuFunction.apply(this);
        }
    }

    private float getHotbarAlpha() {
        long fadeOutProgress = this.lastSelectionTime - Util.getMillis() + SpectatorGui.FADE_OUT_DELAY;
        return Mth.clamp((float) fadeOutProgress / SpectatorGui.FADE_OUT_TIME, 0, 1);
    }

    public void renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.menu != null) {
            float alpha = this.getHotbarAlpha();
            if (alpha <= 0) {
                this.menu.exit();
            } else {
                int x = guiGraphics.guiWidth() / 2;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, -90f);
                int y = Mth.floor((float) guiGraphics.guiHeight() - 22f * alpha);
                SelectionPage selectionPage = this.menu.getCurrentPage();
                this.renderPage(guiGraphics, alpha, x, y, selectionPage);
                guiGraphics.pose().popPose();
            }
        }
    }

    protected void renderPage(GuiGraphics guiGraphics, float alpha, int x, int y, SelectionPage selectionPage) {
        RenderSystem.enableBlend();
        guiGraphics.setColor(1, 1, 1, alpha);
        guiGraphics.blitSprite(SpectatorGui.HOTBAR_SPRITE, x - 91, y, 182, 22);
        if (selectionPage.getSelectedSlot() >= 0) {
            guiGraphics.blitSprite(SpectatorGui.HOTBAR_SELECTION_SPRITE, x - 91 - 1 + selectionPage.getSelectedSlot() * 20, y - 1, 24, 23);
        }

        guiGraphics.setColor(1, 1, 1, 1);

        for (int i = 0; i < 9; i++) {
            this.renderSlot(guiGraphics, i, guiGraphics.guiWidth() / 2 - 90 + i * 20 + 2, y + 3, alpha, selectionPage.getItem(i));
        }

        RenderSystem.disableBlend();
    }

    private void renderSlot(GuiGraphics guiGraphics, int slot, int x, float y, float alpha, SelectionMenuItem selectionMenuItem) {
        if (selectionMenuItem != SelectionMenu.EMPTY_SLOT) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float) x, y, 0);
            float shadeColor = selectionMenuItem.isEnabled() ? 1 : 0.25f;
            guiGraphics.setColor(shadeColor, shadeColor, shadeColor, alpha);
            selectionMenuItem.renderIcon(guiGraphics, alpha);
            guiGraphics.setColor(1, 1, 1, 1);
            guiGraphics.pose().popPose();
            int bigAlpha = (int) (alpha * 255);
            if (bigAlpha > 3 && selectionMenuItem.isEnabled()) {
                Component component = this.minecraft.options.keyHotbarSlots[slot].getTranslatedKeyMessage();
                guiGraphics.drawString(
                        this.minecraft.font, component, x + 19 - 2 - this.minecraft.font.width(component), (int) y + 6 + 3, 16777215 + (bigAlpha << 24));
            }
        }
    }

    public void renderTooltip(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int bigAlpha = (int) (this.getHotbarAlpha() * 255);
        if (bigAlpha > 3 && this.menu != null) {
            SelectionMenuItem selectionMenuItem = this.menu.getSelectedItem();
            Component component = selectionMenuItem == SelectionMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : selectionMenuItem.getName();
            if (component != null) {
                int j = this.minecraft.font.width(component);
                int k = (guiGraphics.guiWidth() - j) / 2;
                int l = guiGraphics.guiHeight() - 35;
                guiGraphics.drawStringWithBackdrop(this.minecraft.font, component, k, l, j, FastColor.ARGB32.color(bigAlpha, -1));
            }
        }
    }

    @Override
    public void onMenuClosed(SelectionMenu menu) {
        this.menu = null;
        this.lastSelectionTime = 0;
    }

    public boolean isMenuActive() {
        return this.menu != null;
    }

    public void onMouseScrolled(int amount) {
        int i = this.menu.getSelectedSlot() + amount;

        while (i >= 0 && i <= 8 && (this.menu.getItem(i) == SelectionMenu.EMPTY_SLOT || !this.menu.getItem(i).isEnabled())) {
            i += amount;
        }

        if (i >= 0 && i <= 8) {
            this.menu.selectSlot(i);
            this.lastSelectionTime = Util.getMillis();
        }
    }

    public void onMouseMiddleClick() {
        this.lastSelectionTime = Util.getMillis();
        if (this.isMenuActive()) {
            int selectedSlot = this.menu.getSelectedSlot();
            if (selectedSlot != SpectatorPage.NO_SELECTION) {
                this.menu.selectSlot(selectedSlot);
            }
        } else {
            this.menu = menuFunction.apply(this);
        }
    }
}
