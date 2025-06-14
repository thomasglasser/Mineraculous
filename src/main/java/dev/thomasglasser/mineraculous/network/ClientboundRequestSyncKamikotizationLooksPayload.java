package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncKamikotizationLooksPayload(UUID senderId, List<Holder<Kamikotization>> kamikotizations) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncKamikotizationLooksPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_kamikotization_looks"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncKamikotizationLooksPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundRequestSyncKamikotizationLooksPayload::senderId,
            Kamikotization.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRequestSyncKamikotizationLooksPayload::kamikotizations,
            ClientboundRequestSyncKamikotizationLooksPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Path folder = MineraculousClientUtils.getGameDirectory().resolve("miraculouslooks").resolve("kamikotizations");
        if (!Files.exists(folder)) {
            return;
        }
        List<FlattenedKamikotizationLookData> looks = new ReferenceArrayList<>();
        for (Holder<Kamikotization> kamikotization : kamikotizations) {
            try {
                FlattenedKamikotizationLookData data = FlattenedKamikotizationLookData.resolve(kamikotization);
                if (data != null) {
                    looks.add(data);
                }
            } catch (IOException e) {
                Mineraculous.LOGGER.error("Failed to resolve kamikotization look for {}", kamikotization, e);
            }
        }
        TommyLibServices.NETWORK.sendToServer(new ServerboundSyncKamikotizationLooksPayload(senderId, looks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
