package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.server.look.LookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSendCachedLookPayload(String hash, byte[] data) implements ExtendedPacketPayload {
    public static final Type<ClientboundSendCachedLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_send_cached_look"));
    public static final StreamCodec<ByteBuf, ClientboundSendCachedLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundSendCachedLookPayload::hash,
            ByteBufCodecs.byteArray(LookManager.MAX_FILE_SIZE), ClientboundSendCachedLookPayload::data,
            ClientboundSendCachedLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        // TODO: Check permissions
        try {
            LookManager.ensureCacheExists(LookLoader.CACHE_PATH);
            Path look = LookLoader.CACHE_PATH.resolve(hash + ".look");
            Files.write(look, data);
            LookLoader.load(look, false);
        } catch (Exception e) {
            MineraculousConstants.LOGGER.warn("Failed to parse look {}", hash, e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
