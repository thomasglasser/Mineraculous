package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

public record ServerboundTameEntityPayload(int id) implements ExtendedPacketPayload {
    public static final Type<ServerboundTameEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_tame_entity"));
    public static final StreamCodec<ByteBuf, ServerboundTameEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundTameEntityPayload::id,
            ServerboundTameEntityPayload::new);

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(id);
        if (entity instanceof TamableAnimal animal)
            animal.tame(player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
