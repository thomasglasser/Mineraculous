package dev.thomasglasser.mineraculous.network;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

public record ClientboundRequestSyncSuitLookPayload(Optional<UUID> senderId, boolean announce, ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {

    public static final Type<ClientboundRequestSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncSuitLookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ClientboundRequestSyncSuitLookPayload::senderId,
            ByteBufCodecs.BOOL, ClientboundRequestSyncSuitLookPayload::announce,
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncSuitLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncSuitLookPayload::look,
            ClientboundRequestSyncSuitLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        String namespace = miraculous.location().getNamespace();
        String type = miraculous.location().getPath();
        File folder = new File(Minecraft.getInstance().gameDirectory, "miraculouslooks" + File.separator + "suits" + File.separator + namespace + File.separator + type);
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
                List<byte[]> convertedFrames = new ArrayList<>();
                for (int i = 1; i <= ClientUtils.getLevel().holderOrThrow(miraculous).value().transformationFrames(); i++) {
                    File frame = new File(folder, look + "_" + i + ".png");
                    if (frame.exists()) {
                        convertedFrames.add(NativeImage.read(frame.toPath().toUri().toURL().openStream()).asByteArray());
                    }
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncSuitLookPayload(senderId, announce, new FlattenedSuitLookData(miraculous, look, convertedModel, convertedImage, convertedFrames)));
            } catch (Exception exception) {
                sendFail();
                Mineraculous.LOGGER.error("Failed to handle clientbound request sync suit look payload", exception);
            }
        } else {
            sendFail();
        }
    }

    private void sendFail() {
        TommyLibServices.NETWORK.sendToServer(new ServerboundReportSuitLookAbsentPayload(senderId, miraculous, look));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
