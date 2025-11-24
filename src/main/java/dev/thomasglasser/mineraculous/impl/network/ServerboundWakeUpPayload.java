package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundWakeUpPayload(UUID targetId, boolean showStealingWarning) implements ExtendedPacketPayload {
    public static final Component STEALING_WARNING = Component.translatable("mineraculous.stealing_warning");

    public static final Type<ServerboundWakeUpPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_wake_up"));
    public static final StreamCodec<ByteBuf, ServerboundWakeUpPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundWakeUpPayload::targetId,
            ByteBufCodecs.BOOL, ServerboundWakeUpPayload::showStealingWarning,
            ServerboundWakeUpPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            target.stopSleepInBed(true, true);
            if (showStealingWarning) {
                target.displayClientMessage(STEALING_WARNING, true);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
