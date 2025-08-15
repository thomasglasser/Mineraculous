package dev.thomasglasser.mineraculous.api.client.gui.screens;

import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an option in the {@link RadialMenuScreen},
 * defining translation key and optional color override.
 */
public interface RadialMenuOption {
    Component displayName();

    default @Nullable Integer colorOverride() {
        return null;
    }

    /**
     * Returns whether the option is enabled for the given stack and holder.
     * Only applies when the screen is created using a {@link RadialMenuProvider}.
     * 
     * @param stack  The {@link ItemStack} being used in the screen
     * @param holder The {@link Player} holding the provided item
     * @return Whether the option is enabled
     */
    default boolean isEnabled(ItemStack stack, Player holder) {
        return true;
    }
}
