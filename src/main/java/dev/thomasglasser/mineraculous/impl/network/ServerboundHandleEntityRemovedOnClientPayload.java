package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.entity.ClientRemovalListener;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundHandleEntityRemovedOnClientPayload(int entityId) implements ExtendedPacketPayload {
    public static final Type<ServerboundHandleEntityRemovedOnClientPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_handle_entity_removed_on_client"));
    public static final StreamCodec<ByteBuf, ServerboundHandleEntityRemovedOnClientPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHandleEntityRemovedOnClientPayload::entityId,
            ServerboundHandleEntityRemovedOnClientPayload::new);

    @Override
    public void handle(Player player) {
        if (player.level().getEntity(entityId) instanceof ClientRemovalListener listener) {
            listener.onRemovedOnClient(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
