package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public record ServerboundOpenVictimKamikotizationChatScreenPayload(UUID targetId, KamikotizationData kamikotizationData, SlotInfo slotInfo) implements ExtendedPacketPayload {

    public static final Type<ServerboundOpenVictimKamikotizationChatScreenPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_open_victim_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenVictimKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenVictimKamikotizationChatScreenPayload::targetId,
            KamikotizationData.STREAM_CODEC, ServerboundOpenVictimKamikotizationChatScreenPayload::kamikotizationData,
            SlotInfo.STREAM_CODEC, ServerboundOpenVictimKamikotizationChatScreenPayload::slotInfo,
            ServerboundOpenVictimKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.level().getPlayerByUUID(targetId) instanceof ServerPlayer target) {
            target.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withPrivateChat(Optional.of(kamikotizationData.kamikoData().owner()), kamikotizationData.kamikoData().faceMaskTexture()).save(target);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenVictimKamikotizationChatScreenPayload(player.getUUID(), kamikotizationData, slotInfo), target);
            target.level().playSound(null, target.blockPosition(), MineraculousSoundEvents.KAMIKOTIZATION_BEGIN.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
