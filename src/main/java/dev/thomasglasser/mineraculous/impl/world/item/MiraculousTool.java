package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.impl.client.gui.tool.ToolModeItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface MiraculousTool {
    List<ToolModeItem> getItemStackToolModes(ItemStack stack);
}
