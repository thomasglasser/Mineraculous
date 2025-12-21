package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class ServerboundActivatePowerPayload implements ExtendedPacketPayload {
    public static final ServerboundActivatePowerPayload INSTANCE = new ServerboundActivatePowerPayload();
    public static final Type<ServerboundActivatePowerPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_activate_power"));
    public static final StreamCodec<ByteBuf, ServerboundActivatePowerPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundActivatePowerPayload() {}

    @Override
    public void handle(Player player) {
        List<Holder<Miraculous>> transformed = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed();
        if (!transformed.isEmpty()) {
            Holder<Miraculous> miraculous = transformed.getFirst();
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (data.transformed() && !data.powerActive()) {
                data.withPowerActive(true).save(miraculous, player);
            }
        } else {
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                if (!data.powerActive()) {
                    data.withPowerActive(true).save(player);
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
