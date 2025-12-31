package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundWakeUpPayload(UUID targetId, Optional<Component> warning) implements ExtendedPacketPayload {
    public static final Type<ServerboundWakeUpPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_wake_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundWakeUpPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundWakeUpPayload::targetId,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, ServerboundWakeUpPayload::warning,
            ServerboundWakeUpPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            target.stopSleepInBed(true, true);
            warning.ifPresent(warning -> target.displayClientMessage(warning, true));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
