package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundRefreshDisplayNamePayload(UUID targetId) implements ExtendedPacketPayload {
    public static final Type<ClientboundRefreshDisplayNamePayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_refresh_display_name"));
    public static final StreamCodec<ByteBuf, ClientboundRefreshDisplayNamePayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundRefreshDisplayNamePayload::targetId,
            ClientboundRefreshDisplayNamePayload::new);

    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null) {
            target.refreshDisplayName();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
