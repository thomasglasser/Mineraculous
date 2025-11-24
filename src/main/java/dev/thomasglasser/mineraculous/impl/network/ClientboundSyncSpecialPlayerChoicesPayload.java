package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ClientboundSyncSpecialPlayerChoicesPayload implements ExtendedPacketPayload {
    public static final ClientboundSyncSpecialPlayerChoicesPayload INSTANCE = new ClientboundSyncSpecialPlayerChoicesPayload();
    public static final Type<ClientboundSyncSpecialPlayerChoicesPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_sync_special_player_choices"));
    public static final StreamCodec<ByteBuf, ClientboundSyncSpecialPlayerChoicesPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundSyncSpecialPlayerChoicesPayload() {}

    // ON CLIENT
    public void handle(@Nullable Player player) {
        MineraculousClientUtils.syncSpecialPlayerChoices();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
