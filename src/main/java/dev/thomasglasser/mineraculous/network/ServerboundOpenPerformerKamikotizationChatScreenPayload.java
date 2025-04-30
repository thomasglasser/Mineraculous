package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public record ServerboundOpenPerformerKamikotizationChatScreenPayload(String performerName, String targetName, UUID targetId) implements ExtendedPacketPayload {

    public static final Type<ServerboundOpenPerformerKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_open_performer_kamikotization_chat_screen"));
    public static final StreamCodec<ByteBuf, ServerboundOpenPerformerKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundOpenPerformerKamikotizationChatScreenPayload::performerName,
            ByteBufCodecs.STRING_UTF8, ServerboundOpenPerformerKamikotizationChatScreenPayload::targetName,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenPerformerKamikotizationChatScreenPayload::targetId,
            ServerboundOpenPerformerKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        // TODO: Fix
//        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(player);
//        tag.putBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, true);
//        TommyLibServices.ENTITY.setPersistentData(player, tag, true);
        TommyLibServices.NETWORK.sendToClient(new ClientboundOpenPerformerKamikotizationChatScreenPayload(performerName, targetName, targetId), (ServerPlayer) player);
        player.level().playSound(null, player.blockPosition(), MineraculousSoundEvents.KAMIKOTIZATION_USE.get(), SoundSource.PLAYERS, 1f, 1f);
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
