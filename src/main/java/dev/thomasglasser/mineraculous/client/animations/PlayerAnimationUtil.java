package dev.thomasglasser.mineraculous.client.animations;

import dev.thomasglasser.mineraculous.network.ClientboundPlayPlayerAnimationPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerAnimationUtil {
    public static void playAnimationToAllClients(Player player, String animationName) {
        if (player instanceof ServerPlayer serverPlayer)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundPlayPlayerAnimationPayload(player.getUUID(), animationName), serverPlayer.server);
    }
}
