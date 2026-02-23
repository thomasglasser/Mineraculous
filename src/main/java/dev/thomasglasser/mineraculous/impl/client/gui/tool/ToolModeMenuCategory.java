package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import com.google.common.collect.Lists;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuCategory;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToolModeMenuCategory implements SelectionMenuCategory {
    private final List<SelectionMenuItem> items;
    public static final Component SELECT = Component.translatable("gui.mineraculous.tool_mode.prompt");

    public ToolModeMenuCategory(ItemStack stack, InteractionHand hand, Player player) {
        if (stack.getItem() instanceof ModeTool tool) {
            List<? extends ModeTool.ToolMode> rawModes = tool.getToolModes(stack, hand, player);
            if (rawModes != null) {
                List<ModeTool.ToolMode> modes = Lists.newArrayList(rawModes);
                int numberOfOptions = modes.size();
                int selectedSlot = numberOfOptions / 2;

                int rotations = 0;
                while (modes.get(selectedSlot) != tool.getToolMode(stack) && rotations < modes.size()) {
                    Collections.rotate(modes, 1);
                    rotations++;
                }
                this.items = ToolModeMenuItem.getToolModeMenuItems(modes);
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
        return SELECT;
    }
}
