package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundCloseKamikotizationChatScreenPayload(boolean cancel) implements ExtendedPacketPayload {
    public static final Type<ClientboundCloseKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_close_kamikotization_chat_screen"));
    public static final StreamCodec<ByteBuf, ClientboundCloseKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundCloseKamikotizationChatScreenPayload::cancel,
            ClientboundCloseKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.closeKamikotizationChatScreen(cancel);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
