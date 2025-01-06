package dev.thomasglasser.mineraculous.network;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.LookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncLookPayload(Optional<UUID> senderId, ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {

    public static final Type<ClientboundRequestSyncLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ClientboundRequestSyncLookPayload::senderId,
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncLookPayload::look,
            ClientboundRequestSyncLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        String namespace = miraculous.location().getNamespace();
        String type = miraculous.location().getPath();
        File folder = new File(Minecraft.getInstance().gameDirectory, "miraculouslooks" + File.separator + namespace + File.separator + type);
        if (!folder.exists()) {
            sendFail();
            return;
        }
        File model = new File(folder, look + ".geo.json");
        File texture = new File(folder, look + ".png");
        if (model.exists() && texture.exists()) {
            try {
                String convertedModel = Files.readString(model.toPath());
                byte[] convertedImage = NativeImage.read(texture.toPath().toUri().toURL().openStream()).asByteArray();
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncLookPayload(senderId, new LookData(miraculous, look, convertedModel, convertedImage)));
            } catch (Exception exception) {
                sendFail();
                Mineraculous.LOGGER.error("Failed to handle clientbound request sync look payload", exception);
            }
        } else {
            sendFail();
        }
    }

    private void sendFail() {
        TommyLibServices.NETWORK.sendToServer(new ServerboundReportLookAbsentPayload(senderId, miraculous, look));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
