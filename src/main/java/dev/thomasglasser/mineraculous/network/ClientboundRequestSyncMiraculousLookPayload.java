package dev.thomasglasser.mineraculous.network;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
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

public record ClientboundRequestSyncMiraculousLookPayload(Optional<UUID> senderId, boolean announce, ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {

    public static final Type<ClientboundRequestSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ClientboundRequestSyncMiraculousLookPayload::senderId,
            ByteBufCodecs.BOOL, ClientboundRequestSyncMiraculousLookPayload::announce,
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncMiraculousLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncMiraculousLookPayload::look,
            ClientboundRequestSyncMiraculousLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        String namespace = miraculous.location().getNamespace();
        String type = miraculous.location().getPath();
        File folder = new File(Minecraft.getInstance().gameDirectory, "miraculouslooks" + File.separator + "miraculous" + File.separator + namespace + File.separator + type);
        if (!folder.exists()) {
            sendFail();
            return;
        }
        File texture = new File(folder, look + ".png");
        if (texture.exists()) {
            try {
                File model = new File(folder, look + ".geo.json");
                String convertedModel = null;
                if (model.exists()) {
                    convertedModel = Files.readString(model.toPath());
                }
                byte[] convertedImage = NativeImage.read(texture.toPath().toUri().toURL().openStream()).asByteArray();
                File transforms = new File(folder, look + ".json");
                String convertedDisplay = null;
                if (transforms.exists()) {
                    convertedDisplay = Files.readString(transforms.toPath());
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncMiraculousLookPayload(senderId, announce, new FlattenedMiraculousLookData(miraculous, look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedDisplay))));
            } catch (Exception exception) {
                sendFail();
                Mineraculous.LOGGER.error("Failed to handle clientbound request sync miraculous look payload", exception);
            }
        } else {
            sendFail();
        }
    }

    private void sendFail() {
        TommyLibServices.NETWORK.sendToServer(new ServerboundReportMiraculousLookAbsentPayload(senderId, miraculous, look));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
