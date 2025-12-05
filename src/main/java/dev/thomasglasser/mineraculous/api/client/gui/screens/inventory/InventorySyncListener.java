package dev.thomasglasser.mineraculous.api.client.gui.screens.inventory;

import net.minecraft.world.entity.player.Player;

/// Calls the {@link InventorySyncListener#onInventorySynced(Player)} method when the player's inventory is synced by a {@link dev.thomasglasser.mineraculous.impl.network.ClientboundSyncInventoryPayload}.
public interface InventorySyncListener {
    void onInventorySynced(Player player);
}
