package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncKamikotizationLookPayload(ResourceKey<Kamikotization> kamikotization) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncKamikotizationLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_kamikotization_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncKamikotizationLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), ClientboundRequestSyncKamikotizationLookPayload::kamikotization,
            ClientboundRequestSyncKamikotizationLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        FlattenedKamikotizationLookData data = FlattenedKamikotizationLookData.flatten(kamikotization);
        if (data != null)
            TommyLibServices.NETWORK.sendToServer(new ServerboundSyncKamikotizationLookPayload(data));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
