package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundRequestInventorySyncPayload(UUID uuid, boolean track) implements ExtendedPacketPayload {
    public static final Type<ServerboundRequestInventorySyncPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_request_inventory_sync"));
    public static final StreamCodec<ByteBuf, ServerboundRequestInventorySyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundRequestInventorySyncPayload::uuid,
            ByteBufCodecs.BOOL, ServerboundRequestInventorySyncPayload::track,
            ServerboundRequestInventorySyncPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(uuid);
        if (target != null) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(target), (ServerPlayer) player);
            if (track) {
                target.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).add(player.getUUID());
            } else {
                target.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).remove(player.getUUID());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
