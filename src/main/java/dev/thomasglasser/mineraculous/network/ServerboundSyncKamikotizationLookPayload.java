package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.ServerLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncKamikotizationLookPayload(FlattenedKamikotizationLookData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundSyncKamikotizationLookPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_kamikotization_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncKamikotizationLookPayload> CODEC = StreamCodec.composite(
            FlattenedKamikotizationLookData.CODEC, ServerboundSyncKamikotizationLookPayload::data,
            ServerboundSyncKamikotizationLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (!MineraculousServerConfig.isCustomizationAllowed(player))
            return;
        ServerLookData.getPlayerKamikotizations().put(player.getUUID(), data);
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizationLookPayload(player.getUUID(), data), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
