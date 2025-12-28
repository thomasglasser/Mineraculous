package dev.thomasglasser.mineraculous.api.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/// A {@link TabNavigationBar} that isn't restricted to the top.
public class ExtendedTabNavigationBar extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 500;
    private static final int HEIGHT = 24;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final LinearLayout layout = LinearLayout.horizontal();
    private int width;
    private int startY;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    ExtendedTabNavigationBar(int width, int startY, TabManager tabManager, Iterable<Tab> tabs) {
        this.width = width;
        this.startY = startY;
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(tabs);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder<TabButton> builder = ImmutableList.builder();

        for (Tab tab : tabs) {
            builder.add(this.layout.addChild(new TabButton(tabManager, tab, 0, HEIGHT)));
        }

        this.tabButtons = builder.build();
    }

    public static Builder builder(TabManager tabManager, int width) {
        return new Builder(tabManager, width);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(focused);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        if (listener instanceof TabButton tabbutton) {
            this.tabManager.setCurrentTab(tabbutton.tab(), true);
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (!this.isFocused()) {
            TabButton tabbutton = this.currentTabButton();
            if (tabbutton != null) {
                return ComponentPath.path(this, ComponentPath.leaf(tabbutton));
            }
        }

        return event instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(event);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Optional<TabButton> optional = this.tabButtons
                .stream()
                .filter(AbstractWidget::isHovered)
                .findFirst()
                .or(() -> Optional.ofNullable(this.currentTabButton()));
        optional.ifPresent(tabButton -> {
            this.narrateListElementPosition(narrationElementOutput.nest(), tabButton);
            tabButton.updateNarration(narrationElementOutput);
        });
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, TabButton tabButton) {
        if (this.tabs.size() > 1) {
            int i = this.tabButtons.indexOf(tabButton);
            if (i != NO_TAB) {
                narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        guiGraphics.blit(Screen.HEADER_SEPARATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.tabButtons.getFirst().getX(), 2, 32, 2);
        int i = this.tabButtons.get(this.tabButtons.size() - 1).getRight();
        guiGraphics.blit(Screen.HEADER_SEPARATOR, i, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.disableBlend();

        for (TabButton tabbutton : this.tabButtons) {
            tabbutton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int i = Math.min(MAX_WIDTH, this.width);
        int j = Mth.roundToward(i / this.tabs.size(), 2);

        for (TabButton tabbutton : this.tabButtons) {
            tabbutton.setWidth(j);
        }

        this.layout.arrangeElements();
        this.layout.setX(0);
        this.layout.setY(startY);
    }

    public void selectTab(int index, boolean playClickSound) {
        if (this.isFocused()) {
            this.setFocused(this.tabButtons.get(index));
        } else {
            this.tabManager.setCurrentTab(this.tabs.get(index), playClickSound);
        }
    }

    public boolean keyPressed(int keycode) {
        if (Screen.hasControlDown()) {
            int i = this.getNextTabIndex(keycode);
            if (i != NO_TAB) {
                this.selectTab(Mth.clamp(i, 0, this.tabs.size() - 1), true);
                return true;
            }
        }

        return false;
    }

    private int getNextTabIndex(int keycode) {
        if (keycode >= 49 && keycode <= 57) {
            return keycode - 49;
        } else {
            if (keycode == 258) {
                int i = this.currentTabIndex();
                if (i != NO_TAB) {
                    int j = Screen.hasShiftDown() ? i - 1 : i + 1;
                    return Math.floorMod(j, this.tabs.size());
                }
            }

            return NO_TAB;
        }
    }

    private int currentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        return this.tabs.indexOf(tab);
    }

    @Nullable
    private TabButton currentTabButton() {
        int i = this.currentTabIndex();
        return i != NO_TAB ? this.tabButtons.get(i) : null;
    }

    public static class Builder {
        private final int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<>();

        private int height = 0;

        Builder(TabManager tabManager, int width) {
            this.tabManager = tabManager;
            this.width = width;
        }

        public Builder addTabs(Tab... tabs) {
            Collections.addAll(this.tabs, tabs);
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public ExtendedTabNavigationBar build() {
            return new ExtendedTabNavigationBar(this.width, this.height, this.tabManager, this.tabs);
        }
    }
}
