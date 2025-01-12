package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncKamikotizationLooksPayload(UUID senderId, List<ResourceKey<Kamikotization>> kamikotizations) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncKamikotizationLooksPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_kamikotization_looks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncKamikotizationLooksPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundRequestSyncKamikotizationLooksPayload::senderId,
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION).apply(ByteBufCodecs.list()), ClientboundRequestSyncKamikotizationLooksPayload::kamikotizations,
            ClientboundRequestSyncKamikotizationLooksPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        File folder = new File(Minecraft.getInstance().gameDirectory, "miraculouslooks" + File.separator + "kamikotizations");
        if (!folder.exists()) {
            return;
        }
        List<FlattenedKamikotizationLookData> looks = new ArrayList<>();
        for (ResourceKey<Kamikotization> kamikotization : kamikotizations) {
            FlattenedKamikotizationLookData data = MineraculousClientEvents.flattenKamikotizationLook(kamikotization);
            if (data != null)
                looks.add(data);
        }
        TommyLibServices.NETWORK.sendToServer(new ServerboundSyncKamikotizationLooksPayload(senderId, looks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
