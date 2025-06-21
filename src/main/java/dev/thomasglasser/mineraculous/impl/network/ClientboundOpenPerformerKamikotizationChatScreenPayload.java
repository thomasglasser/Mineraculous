package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ClientboundOpenPerformerKamikotizationChatScreenPayload(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, UUID targetId) implements ExtendedPacketPayload {

    public static final Type<ClientboundOpenPerformerKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_open_performer_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenPerformerKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundOpenPerformerKamikotizationChatScreenPayload::performerName,
            ByteBufCodecs.STRING_UTF8, ClientboundOpenPerformerKamikotizationChatScreenPayload::targetName,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundOpenPerformerKamikotizationChatScreenPayload::faceMaskTexture,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundOpenPerformerKamikotizationChatScreenPayload::targetId,
            ClientboundOpenPerformerKamikotizationChatScreenPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            MineraculousClientUtils.openPerformerKamikotizationChatScreen(performerName, targetName, faceMaskTexture, target);
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
