package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundSetDeltaMovementPayload(Vec3 vec3, boolean resetFallDistance) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetDeltaMovementPayload> TYPE = new Type<>(Mineraculous.modLoc("set_delta_movement"));
    public static final StreamCodec<ByteBuf, ServerboundSetDeltaMovementPayload> CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.VEC_3, ServerboundSetDeltaMovementPayload::vec3,
            ByteBufCodecs.BOOL, ServerboundSetDeltaMovementPayload::resetFallDistance,
            ServerboundSetDeltaMovementPayload::new);

    @Override
    public void handle(Player player) {
        player.setDeltaMovement(vec3);
        player.hurtMarked = true;
        if (resetFallDistance) {
            player.resetFallDistance();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
