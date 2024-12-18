package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public record ServerboundOpenVictimKamikotizationChatScreenPayload(UUID target, KamikotizationData kamikotizationData) implements ExtendedPacketPayload {
    public static final Type<ServerboundOpenVictimKamikotizationChatScreenPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_open_victim_kamikotization_chat_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenVictimKamikotizationChatScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundOpenVictimKamikotizationChatScreenPayload::target,
            KamikotizationData.STREAM_CODEC, ServerboundOpenVictimKamikotizationChatScreenPayload::kamikotizationData,
            ServerboundOpenVictimKamikotizationChatScreenPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player targetPlayer = player.level().getPlayerByUUID(target);
        if (targetPlayer instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(targetPlayer);
            tag.putBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, true);
            TommyLibServices.ENTITY.setPersistentData(targetPlayer, tag, true);
            TommyLibServices.NETWORK.sendToClient(new ClientboundOpenVictimKamikotizationChatScreenPayload(player.getUUID(), kamikotizationData), serverPlayer);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), MineraculousSoundEvents.KAMIKOTIZATION_USE.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    @Override
    public Type<? extends ExtendedPacketPayload> type() {
        return TYPE;
    }
}
