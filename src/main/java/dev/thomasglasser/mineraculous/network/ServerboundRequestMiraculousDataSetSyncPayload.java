package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ServerboundRequestMiraculousDataSetSyncPayload(int entity) implements ExtendedPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundRequestMiraculousDataSetSyncPayload> TYPE = new Type<>(Mineraculous.modLoc("request_miraculous_data_set_sync"));
    public static final StreamCodec<ByteBuf, ServerboundRequestMiraculousDataSetSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundRequestMiraculousDataSetSyncPayload::entity,
            ServerboundRequestMiraculousDataSetSyncPayload::new);

    public ServerboundRequestMiraculousDataSetSyncPayload(ByteBuf buf) {
        this(buf.readInt());
    }

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.level().getEntity(entity) instanceof LivingEntity livingEntity)
            TommyLibServices.NETWORK.sendToTrackingClients(new ClientboundSyncMiraculousDataSetPayload(livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS), entity), player.level().getServer(), livingEntity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
