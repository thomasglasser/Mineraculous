package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.IOException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncMiraculousLookPayload(ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncMiraculousLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncMiraculousLookPayload::look,
            ClientboundRequestSyncMiraculousLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        try {
            FlattenedMiraculousLookData data = FlattenedMiraculousLookData.resolve(miraculous, look);
            if (data != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncMiraculousLookPayload(miraculous, data));
            }
        } catch (IOException e) {
            Mineraculous.LOGGER.error("Failed to resolve miraculous look {} for {}", look, miraculous, e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
