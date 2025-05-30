package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ServerLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundSyncCustomizationPayload(ResourceKey<Miraculous> key, String name, Optional<FlattenedSuitLookData> suit, Optional<FlattenedMiraculousLookData> miraculous) implements ExtendedPacketPayload {

    public static final Type<ServerboundSyncCustomizationPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_sync_customization"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSyncCustomizationPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSyncCustomizationPayload::key,
            ByteBufCodecs.STRING_UTF8, ServerboundSyncCustomizationPayload::name,
            ByteBufCodecs.optional(FlattenedSuitLookData.CODEC), ServerboundSyncCustomizationPayload::suit,
            ByteBufCodecs.optional(FlattenedMiraculousLookData.CODEC), ServerboundSyncCustomizationPayload::miraculous,
            ServerboundSyncCustomizationPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        MiraculousData data = miraculousesData.get(key).withName(name);
        String suitLook = suit.isPresent() ? suit.get().look() : "";
        data = data.withSuitLook(suitLook);
        if (!suitLook.isEmpty() && (MineraculousServerConfig.isCustomizationAllowed(player) || ServerLookData.getCommonSuits().containsKey(key))) {
            ServerLookData.addPlayerSuit(player.getUUID(), key, suit.get());
            MineraculousEntityEvents.updateAndSyncSuitLook((ServerPlayer) player, key, suit.get());
        }
        String miraculousLook = miraculous.isPresent() ? miraculous.get().look() : "";
        data = data.withMiraculousLook(miraculousLook);
        if (!miraculousLook.isEmpty() && (MineraculousServerConfig.isCustomizationAllowed(player) || ServerLookData.getCommonMiraculouses().containsKey(key))) {
            ServerLookData.addPlayerMiraculous(player.getUUID(), key, miraculous.get());
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousLookPayload(player.getUUID(), key, miraculous.get()), player.getServer());
        }
        miraculousesData.put(player, key, data, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
