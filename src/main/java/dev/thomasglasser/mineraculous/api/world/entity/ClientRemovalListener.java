package dev.thomasglasser.mineraculous.api.world.entity;

import net.minecraft.world.entity.player.Player;

public interface ClientRemovalListener {
    void onRemovedOnClient(Player player);
}
