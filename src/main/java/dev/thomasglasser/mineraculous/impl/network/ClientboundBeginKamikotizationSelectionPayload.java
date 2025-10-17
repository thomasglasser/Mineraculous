package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundBeginKamikotizationSelectionPayload(UUID target, KamikoData kamikoData) implements ExtendedPacketPayload {
    public static final Type<ClientboundBeginKamikotizationSelectionPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_begin_kamikotization_selection"));
    public static final StreamCodec<ByteBuf, ClientboundBeginKamikotizationSelectionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundBeginKamikotizationSelectionPayload::target,
            KamikoData.STREAM_CODEC, ClientboundBeginKamikotizationSelectionPayload::kamikoData,
            ClientboundBeginKamikotizationSelectionPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.beginKamikotizationSelection(player.level().getPlayerByUUID(target), kamikoData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
