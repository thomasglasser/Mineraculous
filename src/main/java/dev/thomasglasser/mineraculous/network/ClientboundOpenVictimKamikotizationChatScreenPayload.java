package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenVictimKamikotizationChatScreenPayload(UUID performer, KamikotizationData kamikotizationData, KamikoData kamikoData) implements ExtendedPacketPayload {

    public static final Type<ClientboundOpenVictimKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_victim_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenVictimKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundOpenVictimKamikotizationChatScreenPayload::performer,
            KamikotizationData.STREAM_CODEC, ClientboundOpenVictimKamikotizationChatScreenPayload::kamikotizationData,
            KamikoData.STREAM_CODEC, ClientboundOpenVictimKamikotizationChatScreenPayload::kamikoData,
            ClientboundOpenVictimKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openKamikotizationChatScreen(player.level().getPlayerByUUID(performer), kamikotizationData);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
