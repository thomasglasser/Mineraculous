package dev.thomasglasser.mineraculous.api.client.gui.screens.inventory;

import net.minecraft.world.entity.player.Player;

public interface InventorySyncTracker {
    void onInventorySynced(Player player);
}
