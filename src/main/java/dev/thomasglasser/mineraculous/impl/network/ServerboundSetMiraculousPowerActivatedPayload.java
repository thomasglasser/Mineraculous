package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetMiraculousPowerActivatedPayload(Holder<Miraculous> miraculous) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetMiraculousPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_miraculous_power_activated"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetMiraculousPowerActivatedPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundSetMiraculousPowerActivatedPayload::miraculous,
            ServerboundSetMiraculousPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
        if (data.transformed() && !data.powerActive()) {
            data.withPowerActive(true).save(miraculous, player, true);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
