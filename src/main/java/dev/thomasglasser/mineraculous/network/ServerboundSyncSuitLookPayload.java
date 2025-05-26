package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncSuitLookPayload(ResourceKey<Miraculous> key, FlattenedSuitLookData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncSuitLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSyncSuitLookPayload::key,
            FlattenedSuitLookData.CODEC, ServerboundSyncSuitLookPayload::data,
            ServerboundSyncSuitLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(player, key, miraculousesData.get(key).withSuitLook(data.look()), false);
        ((FlattenedLookDataHolder) player.getServer().overworld()).mineraculous$addSuitLookData(player.getUUID(), key, data);
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncSuitLookPayload(player.getUUID(), key, data, true), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
