package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncThrownLadybugYoyoDataPayload(int targetId, ThrownLadybugYoyoData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncThrownLadybugYoyoDataPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_thrown_ladybug_yoyo_data"));
    public static final StreamCodec<ByteBuf, ClientboundSyncThrownLadybugYoyoDataPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSyncThrownLadybugYoyoDataPayload::targetId,
            ThrownLadybugYoyoData.STREAM_CODEC, ClientboundSyncThrownLadybugYoyoDataPayload::data,
            ClientboundSyncThrownLadybugYoyoDataPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        System.out.println(data.safeFallTicks());
        player.setData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
