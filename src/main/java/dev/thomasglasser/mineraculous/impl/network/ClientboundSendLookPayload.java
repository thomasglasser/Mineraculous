package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundSendLookPayload(String hash, ServerLookManager.ServerLook look) implements ExtendedPacketPayload {
    public static final Type<ClientboundSendLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_send_look"));
    public static final StreamCodec<ByteBuf, ClientboundSendLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundSendLookPayload::hash,
            ServerLookManager.ServerLook.STREAM_CODEC, ClientboundSendLookPayload::look,
            ClientboundSendLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        // TODO: Check permissions
        try {
            ServerLookManager.ensureCacheExists(LookLoader.CACHE_PATH);
            Path path = LookLoader.CACHE_PATH.resolve(hash + ".look");
            Files.write(path, look.data());
            LookLoader.load(path, look.equippable());
        } catch (Exception e) {
            MineraculousConstants.LOGGER.warn("Failed to parse look {}", hash, e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
