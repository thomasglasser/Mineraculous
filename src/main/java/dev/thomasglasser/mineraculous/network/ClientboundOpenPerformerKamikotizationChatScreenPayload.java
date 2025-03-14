package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenPerformerKamikotizationChatScreenPayload(String targetName, String performerName, UUID targetId) implements ExtendedPacketPayload {

    public static final Type<ClientboundOpenPerformerKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_performer_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenPerformerKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundOpenPerformerKamikotizationChatScreenPayload::targetName,
            ByteBufCodecs.STRING_UTF8, ClientboundOpenPerformerKamikotizationChatScreenPayload::performerName,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundOpenPerformerKamikotizationChatScreenPayload::targetId,
            ClientboundOpenPerformerKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openKamikotizationChatScreen(targetName, performerName, player.level().getPlayerByUUID(targetId));
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
