package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToolModeMenuItem<T> implements SelectionMenuItem {
    protected final Component name;
    protected final ResourceLocation iconTexture;
    protected final ModeTool.ToolMode toolMode;

    public ToolModeMenuItem(ModeTool.ToolMode mode) {
        name = mode.displayName();
        iconTexture = mode.getIcon();
        toolMode = mode;
    }

    public static List<SelectionMenuItem> getToolModeMenuItems(List<ModeTool.ToolMode> modes) {
        return modes.stream()
                .map(ToolModeMenuItem::new)
                .collect(ImmutableList.toImmutableList());
    }

    public ResourceLocation getIconTexture() {
        return iconTexture;
    }

    public ModeTool.ToolMode getToolMode() {
        return toolMode;
    }

    @Override
    public void selectItem(SelectionMenu menu) {}

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float alpha) {
        guiGraphics.blitSprite(iconTexture, 2, 2, 10, 10);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
