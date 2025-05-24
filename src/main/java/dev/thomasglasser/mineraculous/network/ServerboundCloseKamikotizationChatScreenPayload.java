package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundCloseKamikotizationChatScreenPayload(UUID playerId, boolean cancel) implements ExtendedPacketPayload {
    public static final Type<ServerboundCloseKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_close_kamikotization_chat_screen"));
    public static final StreamCodec<ByteBuf, ServerboundCloseKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundCloseKamikotizationChatScreenPayload::playerId,
            ByteBufCodecs.BOOL, ServerboundCloseKamikotizationChatScreenPayload::cancel,
            ServerboundCloseKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player player1 = player.level().getPlayerByUUID(playerId);
        if (player1 instanceof ServerPlayer serverPlayer) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundRemoteCloseKamikotizationChatScreenPayload(cancel), serverPlayer);
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
