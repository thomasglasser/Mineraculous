package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundRemoteCloseKamikotizationChatScreenPayload(boolean cancel) implements ExtendedPacketPayload {
    public static final Type<ClientboundRemoteCloseKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_remote_close_kamikotization_chat_screen"));
    public static final StreamCodec<ByteBuf, ClientboundRemoteCloseKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundRemoteCloseKamikotizationChatScreenPayload::cancel,
            ClientboundRemoteCloseKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.remoteCloseKamikotizationChatScreen(cancel);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
