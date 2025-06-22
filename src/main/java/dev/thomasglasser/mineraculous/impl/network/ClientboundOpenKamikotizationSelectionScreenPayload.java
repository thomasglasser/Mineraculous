package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenKamikotizationSelectionScreenPayload(UUID target, KamikoData kamikoData) implements ExtendedPacketPayload {
    public static final Type<ClientboundOpenKamikotizationSelectionScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_kamikotization_selection_screen"));
    public static final StreamCodec<ByteBuf, ClientboundOpenKamikotizationSelectionScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundOpenKamikotizationSelectionScreenPayload::target,
            KamikoData.STREAM_CODEC, ClientboundOpenKamikotizationSelectionScreenPayload::kamikoData,
            ClientboundOpenKamikotizationSelectionScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openKamikotizationSelectionScreen(player.level().getPlayerByUUID(target), kamikoData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
