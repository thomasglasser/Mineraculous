package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record ServerboundSendOffhandSwingPayload() implements ExtendedPacketPayload {
    public static final ServerboundSendOffhandSwingPayload INSTANCE = new ServerboundSendOffhandSwingPayload();
    public static final Type<ServerboundSendOffhandSwingPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_send_offhand_swing"));
    public static final StreamCodec<ByteBuf, ServerboundSendOffhandSwingPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        player.getOffhandItem().onEntitySwing(player, InteractionHand.OFF_HAND);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
