package dev.thomasglasser.mineraculous.api.client.gui.screens;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/// Disables the rendering of mob effects and adds some handlers for special behavior.
public interface ExternalMenuScreen {
    /// Returns whether the item in the provided slot can be selected and picked up.
    default boolean canPickUp(Slot slot, Player target, AbstractContainerMenu menu) {
        return slot.hasItem();
    }

    /// Called when an item in the provided slot is picked up.
    void pickUp(Slot slot, Player target, AbstractContainerMenu menu);

    /// Called when the screen is closed, specifying if the operation was completed or canceled.
    void onClose(boolean cancel);
}
