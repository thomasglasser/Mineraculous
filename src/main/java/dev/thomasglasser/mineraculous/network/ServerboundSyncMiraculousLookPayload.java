package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ServerLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncMiraculousLookPayload(ResourceKey<Miraculous> miraculous, FlattenedMiraculousLookData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSyncMiraculousLookPayload::miraculous,
            FlattenedMiraculousLookData.CODEC, ServerboundSyncMiraculousLookPayload::data,
            ServerboundSyncMiraculousLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(player, miraculous, miraculousesData.get(miraculous).withMiraculousLook(data.look()), false);
        ServerLookData.addPlayerMiraculous(player.getUUID(), miraculous, data);
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousLookPayload(player.getUUID(), miraculous, data), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
