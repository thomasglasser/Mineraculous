package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSendLookPayload(String hash, byte[] data) implements ExtendedPacketPayload {
    public static final Type<ServerboundSendLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_send_look"));
    public static final StreamCodec<ByteBuf, ServerboundSendLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundSendLookPayload::hash,
            ByteBufCodecs.byteArray(ServerLookManager.MAX_FILE_SIZE), ServerboundSendLookPayload::data,
            ServerboundSendLookPayload::new);

    @Override
    public void handle(Player player) {
        // TODO: Check permissions
        ServerLookManager.saveLook(hash, data);
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSendCachedLookPayload(hash, data), player.getServer());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
