package dev.thomasglasser.mineraculous.network;

import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ClientboundRequestSyncSuitLookPayload(ResourceKey<Miraculous> miraculous, String look) implements ExtendedPacketPayload {
    public static final Type<ClientboundRequestSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_request_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRequestSyncSuitLookPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ClientboundRequestSyncSuitLookPayload::miraculous,
            ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncSuitLookPayload::look,
            ClientboundRequestSyncSuitLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        String namespace = miraculous.location().getNamespace();
        String type = miraculous.location().getPath();
        Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve("miraculouslooks").resolve("suits").resolve(namespace).resolve(type);
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
                List<byte[]> convertedFrames = new ArrayList<>();
                List<byte[]> convertedGlowmaskFrames = new ArrayList<>();
                for (int i = 1; i <= ClientUtils.getLevel().holderOrThrow(miraculous).value().transformationFrames(); i++) {
                    Path frame = texture.resolveSibling(look + "_" + i + ".png");
                    if (Files.exists(frame)) {
                        convertedFrames.add(NativeImage.read(frame.toUri().toURL().openStream()).asByteArray());
                    }
                    Path glowmaskFrame = texture.resolveSibling(look + "_" + i + "_glowmask.png");
                    if (Files.exists(glowmaskFrame)) {
                        convertedGlowmaskFrames.add(NativeImage.read(glowmaskFrame.toUri().toURL().openStream()).asByteArray());
                    }
                }
                Path animations = texture.resolveSibling(look + ".animation.json");
                String convertedAnimations = null;
                if (Files.exists(animations)) {
                    convertedAnimations = Files.readString(animations);
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncSuitLookPayload(miraculous, new FlattenedSuitLookData(look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), convertedFrames, convertedGlowmaskFrames, Optional.ofNullable(convertedAnimations))));
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
