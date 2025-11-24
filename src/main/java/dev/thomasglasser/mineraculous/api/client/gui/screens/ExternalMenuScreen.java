package dev.thomasglasser.mineraculous.api.client.gui.screens;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public interface ExternalMenuScreen {
    default boolean canPickUp(Slot slot, Player target, AbstractContainerMenu menu) {
        return slot.hasItem();
    }

    void pickUp(Slot slot, Player target, AbstractContainerMenu menu);

    void onClose(boolean exit);
}
