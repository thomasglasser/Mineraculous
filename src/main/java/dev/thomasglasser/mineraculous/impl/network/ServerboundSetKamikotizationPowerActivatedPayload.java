package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetKamikotizationPowerActivatedPayload() implements ExtendedPacketPayload {
    public static final Type<ServerboundSetKamikotizationPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_kamikotization_power_activated"));
    public static final ServerboundSetKamikotizationPowerActivatedPayload INSTANCE = new ServerboundSetKamikotizationPowerActivatedPayload();
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetKamikotizationPowerActivatedPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            if (!data.powerActive()) {
                data.withPowerActive(true).save(player, true);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
