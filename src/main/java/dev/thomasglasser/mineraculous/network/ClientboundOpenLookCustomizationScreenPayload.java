package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenLookCustomizationScreenPayload(Optional<UUID> senderId, boolean announce, Holder<Miraculous> miraculous, Map<String, FlattenedSuitLookData> serverSuits, Map<String, FlattenedMiraculousLookData> serverMiraculous) implements ExtendedPacketPayload {

    public static final Type<ClientboundOpenLookCustomizationScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_look_customization_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenLookCustomizationScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ClientboundOpenLookCustomizationScreenPayload::senderId,
            ByteBufCodecs.BOOL, ClientboundOpenLookCustomizationScreenPayload::announce,
            Miraculous.STREAM_CODEC, ClientboundOpenLookCustomizationScreenPayload::miraculous,
            ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ByteBufCodecs.STRING_UTF8, FlattenedSuitLookData.CODEC), ClientboundOpenLookCustomizationScreenPayload::serverSuits,
            ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ByteBufCodecs.STRING_UTF8, FlattenedMiraculousLookData.CODEC), ClientboundOpenLookCustomizationScreenPayload::serverMiraculous,
            ClientboundOpenLookCustomizationScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.openLookCustomizationScreen(miraculous, serverSuits, serverMiraculous);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
