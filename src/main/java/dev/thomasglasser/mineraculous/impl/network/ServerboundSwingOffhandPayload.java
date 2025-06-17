package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record ServerboundSwingOffhandPayload() implements ExtendedPacketPayload {
    public static final ServerboundSwingOffhandPayload INSTANCE = new ServerboundSwingOffhandPayload();
    public static final Type<ServerboundSwingOffhandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_swing_offhand"));
    public static final StreamCodec<ByteBuf, ServerboundSwingOffhandPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        player.getOffhandItem().onEntitySwing(player, InteractionHand.OFF_HAND);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
