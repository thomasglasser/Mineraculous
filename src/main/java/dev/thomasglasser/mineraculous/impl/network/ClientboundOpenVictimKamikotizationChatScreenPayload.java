package dev.thomasglasser.mineraculous.impl.network;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.entity.curio.CuriosData;
import dev.thomasglasser.mineraculous.api.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenVictimKamikotizationChatScreenPayload(UUID performer, KamikotizationData kamikotizationData, Either<Integer, CuriosData> slotInfo) implements ExtendedPacketPayload {

    public static final Type<ClientboundOpenVictimKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_victim_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenVictimKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundOpenVictimKamikotizationChatScreenPayload::performer,
            KamikotizationData.STREAM_CODEC, ClientboundOpenVictimKamikotizationChatScreenPayload::kamikotizationData,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), ClientboundOpenVictimKamikotizationChatScreenPayload::slotInfo,
            ClientboundOpenVictimKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openReceiverKamikotizationChatScreen(performer, kamikotizationData, slotInfo);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
