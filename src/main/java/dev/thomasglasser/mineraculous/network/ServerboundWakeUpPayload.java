package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundWakeUpPayload(UUID target, boolean wokenFromStealing) implements ExtendedPacketPayload {
    public static final String STEALING_WARNING_KEY = "mineraculous.stealing_warning";

    public static final Type<ServerboundWakeUpPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_wake_up"));
    public static final StreamCodec<ByteBuf, ServerboundWakeUpPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundWakeUpPayload::target,
            ByteBufCodecs.BOOL, ServerboundWakeUpPayload::wokenFromStealing,
            ServerboundWakeUpPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player p = player.level().getPlayerByUUID(target);
        if (p != null) {
            p.stopSleepInBed(true, true);
            if (wokenFromStealing) {
                p.displayClientMessage(Component.translatable(STEALING_WARNING_KEY), true);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
