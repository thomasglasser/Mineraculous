package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
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

public record ServerboundOpenVictimKamikotizationChatScreenPayload(UUID targetId, KamikotizationData kamikotizationData, Either<Integer, CuriosData> slotInfo) implements ExtendedPacketPayload {

    public static final Type<ServerboundOpenVictimKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_open_victim_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenVictimKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenVictimKamikotizationChatScreenPayload::targetId,
            KamikotizationData.STREAM_CODEC, ServerboundOpenVictimKamikotizationChatScreenPayload::kamikotizationData,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), ServerboundOpenVictimKamikotizationChatScreenPayload::slotInfo,
            ServerboundOpenVictimKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.level().getPlayerByUUID(targetId) instanceof ServerPlayer target) {
            player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withPrivateChat(Optional.of(kamikotizationData.kamikoData().owner()), kamikotizationData.kamikoData().faceMaskTexture()).save(player, true);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenVictimKamikotizationChatScreenPayload(player.getUUID(), kamikotizationData, slotInfo), target);
            target.level().playSound(null, target.blockPosition(), MineraculousSoundEvents.KAMIKOTIZATION_USE.get(), SoundSource.PLAYERS, 1f, 1f);
            // TODO: Turn item purple/black?
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
