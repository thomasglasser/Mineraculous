package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncLadybugYoyoPayload(Optional<UUID> uuid) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncLadybugYoyoPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_ladybug_yoyo"));
    public static final StreamCodec<ByteBuf, ClientboundSyncLadybugYoyoPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ClientboundSyncLadybugYoyoPayload::uuid,
            ClientboundSyncLadybugYoyoPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        player.setData(MineraculousAttachmentTypes.LADYBUG_YOYO, uuid);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
