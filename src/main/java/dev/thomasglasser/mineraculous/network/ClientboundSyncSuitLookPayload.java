package dev.thomasglasser.mineraculous.network;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public record ClientboundSyncSuitLookPayload(UUID targetId, FlattenedSuitLookData data, boolean override) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncSuitLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_suit_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncSuitLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncSuitLookPayload::targetId,
            FlattenedSuitLookData.CODEC, ClientboundSyncSuitLookPayload::data,
            ByteBufCodecs.BOOL, ClientboundSyncSuitLookPayload::override,
            ClientboundSyncSuitLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        try {
            BakedGeoModel model = null;
            if (data.model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, data.model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/suits/" + target.getStringUUID() + "/" + data.miraculous().location().getNamespace() + "/" + data.miraculous().location().getPath() + "/" + data.look() + ".png");
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(NativeImage.read(data.pixels())));
            List<ResourceLocation> frames = new ArrayList<>();
            for (int i = 0; i < data.frames().size(); i++) {
                ResourceLocation frame = Mineraculous.modLoc("textures/miraculouslooks/suits/" + target.getStringUUID() + "/" + data.miraculous().location().getNamespace() + "/" + data.miraculous().location().getPath() + "/" + data.look() + "_" + (i + 1) + ".png");
                frames.add(frame);
                Minecraft.getInstance().getTextureManager().register(frame, new DynamicTexture(NativeImage.read(data.frames().get(i))));
            }
            target.getData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS).put(data.miraculous(), data.look(), new SuitLookData(Optional.ofNullable(model), texture, frames));
            if (override) {
                MiraculousDataSet miraculousDataSet = target.getData(MineraculousAttachmentTypes.MIRACULOUS);
                miraculousDataSet.put(target, data.miraculous(), miraculousDataSet.get(data.miraculous()).withSuitLook(data.look()), false);
            }
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle clientbound sync suit look payload", e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
