package dev.thomasglasser.mineraculous.network;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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

public record ClientboundSyncMiraculousLookPayload(UUID targetId, FlattenedMiraculousLookData data, boolean override) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncMiraculousLookPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_miraculous_look"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncMiraculousLookPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundSyncMiraculousLookPayload::targetId,
            FlattenedMiraculousLookData.CODEC, ClientboundSyncMiraculousLookPayload::data,
            ByteBufCodecs.BOOL, ClientboundSyncMiraculousLookPayload::override,
            ClientboundSyncMiraculousLookPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        try {
            BakedGeoModel model = null;
            if (data.model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, data.model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/miraculous" + target.getStringUUID() + "/" + data.miraculous().location().getNamespace() + "/" + data.miraculous().location().getPath() + "/" + data.look() + ".png");
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(NativeImage.read(data.pixels())));
            ItemTransforms transforms = null;
            if (data.transforms().isPresent())
                transforms = BlockModel.fromString(data.transforms().get()).getTransforms();
            target.getData(MineraculousAttachmentTypes.MIRACULOUS_MIRACULOUS_LOOKS).put(data.miraculous(), data.look(), new MiraculousLookData(Optional.ofNullable(model), texture, data.glowmaskPixels(), Optional.ofNullable(transforms)));
            if (override) {
                MiraculousDataSet miraculousDataSet = target.getData(MineraculousAttachmentTypes.MIRACULOUS);
                miraculousDataSet.put(target, data.miraculous(), miraculousDataSet.get(data.miraculous()).withMiraculousLook(data.look()), false);
            }
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle clientbound sync miraculous look payload", e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
