package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundRevertConvertedEntityPayload(UUID entityId) implements ExtendedPacketPayload {
    public static final Type<ServerboundRevertConvertedEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_revert_converted_entity"));
    public static final StreamCodec<ByteBuf, ServerboundRevertConvertedEntityPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundRevertConvertedEntityPayload::entityId,
            ServerboundRevertConvertedEntityPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        AbilityReversionEntityData.get(level).revertConversion(entityId, level);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
