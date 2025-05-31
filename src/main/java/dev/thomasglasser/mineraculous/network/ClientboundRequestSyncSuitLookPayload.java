package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.IOException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncSuitLookPayload(ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncSuitLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncSuitLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncSuitLookPayload::look,
            ClientboundRequestSyncSuitLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        try {
            FlattenedSuitLookData data = FlattenedSuitLookData.resolve(miraculous, look);
            if (data != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncSuitLookPayload(miraculous, data));
            }
        } catch (IOException e) {
            Mineraculous.LOGGER.error("Failed to resolve suit look {} for {}", look, miraculous, e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
