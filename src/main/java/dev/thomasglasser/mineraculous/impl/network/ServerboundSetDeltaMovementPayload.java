package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record ServerboundSetDeltaMovementPayload(int entityId, Vector3f vec3) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetDeltaMovementPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_delta_movement"));
    public static final StreamCodec<ByteBuf, ServerboundSetDeltaMovementPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetDeltaMovementPayload::entityId,
            ByteBufCodecs.VECTOR3F, ServerboundSetDeltaMovementPayload::vec3,
            ServerboundSetDeltaMovementPayload::new);

    @Override
    public void handle(Player player) {
        Entity ent = player.level().getEntity(entityId);
        if (ent != null) {
            ent.setDeltaMovement(new Vec3(vec3));
            ent.hurtMarked = true;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
