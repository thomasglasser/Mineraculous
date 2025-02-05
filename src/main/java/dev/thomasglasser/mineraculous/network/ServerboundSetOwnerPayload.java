package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetOwnerPayload(int entityId, Optional<Integer> ownerId) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetOwnerPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_owner"));
    public static final StreamCodec<ByteBuf, ServerboundSetOwnerPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetOwnerPayload::entityId,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ServerboundSetOwnerPayload::ownerId,
            ServerboundSetOwnerPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(ownerId.isPresent() && player.level().getEntity(ownerId.get()) instanceof Entity e ? e.getUUID() : null);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
