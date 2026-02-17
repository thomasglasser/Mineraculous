package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ToolMode {
    /**
     * Returns whether the tool mode is enabled for the given stack and holder.
     * 
     * @param stack  The {@link ItemStack} tool being used.
     * @param holder The {@link Player} holding the provided item
     * @return Whether the option is enabled
     */
    default boolean isEnabled(ItemStack stack, Player holder) {
        return true;
    }
}
