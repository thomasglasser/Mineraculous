package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundRequestMiraculousDataSetSyncPayload(int entityId) implements ExtendedPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundRequestMiraculousDataSetSyncPayload> TYPE = new Type<>(Mineraculous.modLoc("request_miraculous_data_set_sync"));
    public static final StreamCodec<ByteBuf, ServerboundRequestMiraculousDataSetSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundRequestMiraculousDataSetSyncPayload::entityId,
            ServerboundRequestMiraculousDataSetSyncPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entityId, MineraculousAttachmentTypes.MIRACULOUSES, entity.getData(MineraculousAttachmentTypes.MIRACULOUSES)), player.level().getServer());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
