package dev.thomasglasser.mineraculous.mixin.minecraft.server.network;

import dev.thomasglasser.mineraculous.network.ClientboundSyncInventoryPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCarriedItem", at = @At("TAIL"))
    private void updateInventoryOnSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        for (UUID uuid : player.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS)) {
            if (player.level().getPlayerByUUID(uuid) instanceof ServerPlayer tracker) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(player), tracker);
            }
        }
    }
}
