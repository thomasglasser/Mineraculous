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
    public static final Component PROMPT = Component.translatable("gui.mineraculous.tool_mode.prompt");

    public ToolModeMenuCategory(ItemStack stack) {
        if (stack.getItem() instanceof MiraculousTool tool) {
            List<ToolModeItem> rawModes = tool.getToolModes(stack, Minecraft.getInstance().player);
            if (rawModes != null) {
                List<ToolModeItem> modes = new ArrayList<>(rawModes);
                int numberOfOptions = modes.size();
                int selectedSlot = numberOfOptions / 2;

                while (tool.getToolMode(modes.get(selectedSlot).getItemStack()) != tool.getToolMode(stack)) {
                    java.util.Collections.rotate(modes, 1);
                }
                this.items = new ArrayList<>(modes);
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
        return PROMPT;
    }
}
