package dev.thomasglasser.mineraculous.network;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.LookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;

public record ClientboundSyncLookPayload(LookData data) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncLookPayload> CODEC = StreamCodec.composite(
            LookData.CODEC, ClientboundSyncLookPayload::data,
            ClientboundSyncLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        try {
            NativeImage image = NativeImage.read(data.pixels());
            JsonObject model = GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, data.model(), JsonObject.class);

            // TODO: Turn this into a GeoModel and RL that can be used in the armor renderer and save
            player.displayClientMessage(Component.literal("I got it! " + data.look()), false);

            MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            miraculousDataSet.put(player, data.miraculous(), miraculousDataSet.get(data.miraculous()).withLook(data.look()), false);
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle clientbound sync look payload", e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
