package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuCategory;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousTool;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ToolModeMenuCategory implements SelectionMenuCategory {
    private final List<SelectionMenuItem> items;
    public static final Component TARGET_PROMPT = Component.translatable("kamiko_menu.teleport.prompt");

    public ToolModeMenuCategory(ItemStack stack) {
        if (stack.getItem() instanceof MiraculousTool tool) {

            List<ToolModeItem> rawModes = tool.getToolModes(stack, Minecraft.getInstance().player);
            if (rawModes != null) {
                this.items = new ArrayList<>(rawModes);
            } else {
                this.items = List.of();
            }

        } else {
            this.items = List.of();
        }
    }

    @Override
    public List<SelectionMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TARGET_PROMPT;
    }
}
