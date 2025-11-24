package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundOpenPerformerKamikotizationChatScreenPayload(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, UUID targetId) implements ExtendedPacketPayload {

    public static final Type<ServerboundOpenPerformerKamikotizationChatScreenPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_open_performer_kamikotization_chat_screen"));
    public static final StreamCodec<ByteBuf, ServerboundOpenPerformerKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundOpenPerformerKamikotizationChatScreenPayload::performerName,
            ByteBufCodecs.STRING_UTF8, ServerboundOpenPerformerKamikotizationChatScreenPayload::targetName,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ServerboundOpenPerformerKamikotizationChatScreenPayload::faceMaskTexture,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenPerformerKamikotizationChatScreenPayload::targetId,
            ServerboundOpenPerformerKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withPrivateChat(Optional.of(targetId), faceMaskTexture).save(player);
        TommyLibServices.NETWORK.sendToClient(new ClientboundOpenPerformerKamikotizationChatScreenPayload(performerName, targetName, faceMaskTexture, targetId), (ServerPlayer) player);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
