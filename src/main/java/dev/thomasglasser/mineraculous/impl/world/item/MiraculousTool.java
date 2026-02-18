package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.impl.client.gui.tool.ToolModeItem;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MiraculousTool {
    List<ToolModeItem> getToolModes(ItemStack stack, Player holder);

    default boolean canOpenToolModeMenu(ItemStack stack) {
        return true;
    }

    ToolMode getToolMode(ItemStack stack);

    void setToolMode(ItemStack stack, ToolMode mode);

    interface ToolMode {

    }
}
