package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public record ServerboundSetDeltaMovementPayload(Vector3f vec3, boolean resetFallDistance) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetDeltaMovementPayload> TYPE = new Type<>(Mineraculous.modLoc("set_delta_movement"));
    public static final StreamCodec<ByteBuf, ServerboundSetDeltaMovementPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, ServerboundSetDeltaMovementPayload::vec3,
            ByteBufCodecs.BOOL, ServerboundSetDeltaMovementPayload::resetFallDistance,
            ServerboundSetDeltaMovementPayload::new);

    @Override
    public void handle(Player player) {
        double x = vec3().x;
        double y = vec3().y;
        double z = vec3().z;
        player.setDeltaMovement(x, y, z);
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
