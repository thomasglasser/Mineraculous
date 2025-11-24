package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenMiraculousTransferScreenPayload(int kwamiId) implements ExtendedPacketPayload {
    public static final Type<ClientboundOpenMiraculousTransferScreenPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_open_miraculous_transfer_screen"));
    public static final StreamCodec<ByteBuf, ClientboundOpenMiraculousTransferScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundOpenMiraculousTransferScreenPayload::kwamiId,
            ClientboundOpenMiraculousTransferScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openMiraculousTransferScreen(kwamiId);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
