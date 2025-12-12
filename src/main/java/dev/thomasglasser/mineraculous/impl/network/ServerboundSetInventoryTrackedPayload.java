package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetInventoryTrackedPayload(UUID uuid, boolean track) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetInventoryTrackedPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_inventory_tracked"));
    public static final StreamCodec<ByteBuf, ServerboundSetInventoryTrackedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundSetInventoryTrackedPayload::uuid,
            ByteBufCodecs.BOOL, ServerboundSetInventoryTrackedPayload::track,
            ServerboundSetInventoryTrackedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(uuid);
        if (target != null) {
            if (track) {
                target.getData(MineraculousAttachmentTypes.INVENTORY_TRACKERS).add(player.getUUID());
                TommyLibServices.NETWORK.sendToClient(new ClientboundSyncInventoryPayload(target), (ServerPlayer) player);
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
