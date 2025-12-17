package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ServerboundRequestLookPayload(String hash) implements ExtendedPacketPayload {
    public static final Type<ServerboundRequestLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_request_look"));
    public static final StreamCodec<ByteBuf, ServerboundRequestLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundRequestLookPayload::hash,
            ServerboundRequestLookPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        // TODO: Check permissions
        byte[] look = ServerLookManager.getLookData(hash);
        if (look != null) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSendLookPayload(hash, look), (ServerPlayer) player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
