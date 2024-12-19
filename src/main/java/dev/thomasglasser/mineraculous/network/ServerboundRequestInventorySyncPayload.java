package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundRequestInventorySyncPayload(UUID uuid) implements ExtendedPacketPayload {
    public static final Type<ServerboundRequestInventorySyncPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_request_inventory_sync"));
    public static final StreamCodec<ByteBuf, ServerboundRequestInventorySyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundRequestInventorySyncPayload::uuid,
            ServerboundRequestInventorySyncPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(uuid);
        if (target != null) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncInventoryPayload(target), player.getServer());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
