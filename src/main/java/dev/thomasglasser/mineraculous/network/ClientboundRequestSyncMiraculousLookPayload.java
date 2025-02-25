package dev.thomasglasser.mineraculous.network;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncMiraculousLookPayload(ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncMiraculousLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncMiraculousLookPayload::look,
            ClientboundRequestSyncMiraculousLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        String namespace = miraculous.location().getNamespace();
        String type = miraculous.location().getPath();
        Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve("miraculouslooks").resolve("miraculous").resolve(namespace).resolve(type);
        if (!Files.exists(folder)) {
            return;
        }
        Path texture = folder.resolve(look + ".png");
        if (Files.exists(texture)) {
            try {
                Path model = texture.resolveSibling(look + ".geo.json");
                String convertedModel = null;
                if (Files.exists(model)) {
                    convertedModel = Files.readString(model);
                }
                byte[] convertedImage = NativeImage.read(texture.toUri().toURL().openStream()).asByteArray();
                Path glowmask = texture.resolveSibling(look + "_glowmask.png");
                byte[] convertedGlowmask = null;
                if (Files.exists(glowmask)) {
                    convertedGlowmask = NativeImage.read(glowmask.toUri().toURL().openStream()).asByteArray();
                }
                Path transforms = texture.resolveSibling(look + ".json");
                String convertedDisplay = null;
                if (Files.exists(transforms)) {
                    convertedDisplay = Files.readString(transforms);
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncMiraculousLookPayload(miraculous, new FlattenedMiraculousLookData(look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedDisplay))));
            } catch (Exception exception) {
                Mineraculous.LOGGER.error("Failed to handle clientbound request sync suit look payload", exception);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
