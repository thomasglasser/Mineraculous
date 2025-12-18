package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestLookPayload(String hash) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestLookPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_request_look"));
    public static final StreamCodec<ByteBuf, ClientboundRequestLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundRequestLookPayload::hash,
            ClientboundRequestLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        // TODO: Check permissions for allowed
        Path path = LookManager.getPath(hash);
        if (path != null) {
            try {
                byte[] data;
                if (Files.isDirectory(path)) {
                    data = LookLoader.zipFolderToBytes(path);
                } else {
                    data = Files.readAllBytes(path);
                }

                TommyLibServices.NETWORK.sendToServer(new ServerboundSendLookPayload(hash, data));
            } catch (IOException e) {
                MineraculousConstants.LOGGER.warn("Failed to send look {} to server: {}", hash, e.getMessage());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
