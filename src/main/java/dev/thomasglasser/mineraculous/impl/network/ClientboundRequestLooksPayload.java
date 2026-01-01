package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.look.ClientLookManager;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestLooksPayload(Set<String> hashes) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestLooksPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_request_looks"));
    public static final StreamCodec<ByteBuf, ClientboundRequestLooksPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ObjectOpenHashSet::new, ByteBufCodecs.STRING_UTF8), ClientboundRequestLooksPayload::hashes,
            ClientboundRequestLooksPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        for (String hash : hashes) {
            Path path = ClientLookManager.getEquippablePath(hash);
            if (path != null) {
                try {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSendLookPayload(hash, Files.readAllBytes(path)));
                } catch (IOException e) {
                    MineraculousConstants.LOGGER.warn("Failed to send look {} to server: {}", hash, e.getMessage());
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
