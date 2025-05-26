package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.codec.ExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public record FlattenedSuitLookData(String look, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, List<byte[]> frames, List<byte[]> glowmaskFrames, Optional<String> animations) {

    public static final StreamCodec<ByteBuf, FlattenedSuitLookData> CODEC = ExtraStreamCodecs.composite(
            ByteBufCodecs.STRING_UTF8, FlattenedSuitLookData::look,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedSuitLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedSuitLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedSuitLookData::glowmaskPixels,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedSuitLookData::frames,
            ByteBufCodecs.BYTE_ARRAY.apply(ByteBufCodecs.list()), FlattenedSuitLookData::glowmaskFrames,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedSuitLookData::animations,
            FlattenedSuitLookData::new);
    public SuitLookData unpack(ResourceKey<Miraculous> miraculous, Player target) {
        try {
            BakedGeoModel model = null;
            if (model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/suits/" + target.getStringUUID() + "/" + miraculous.location().getNamespace() + "/" + miraculous.location().getPath() + "/" + look() + ".png");
            MineraculousClientUtils.registerDynamicTexture(texture, pixels());
            List<ResourceLocation> frames = new ArrayList<>();
            for (int i = 0; i < frames().size(); i++) {
                ResourceLocation frame = Mineraculous.modLoc("textures/miraculouslooks/suits/" + target.getStringUUID() + "/" + miraculous.location().getNamespace() + "/" + miraculous.location().getPath() + "/" + look() + "_" + (i + 1) + ".png");
                frames.add(frame);
                MineraculousClientUtils.registerDynamicTexture(frame, frames().get(i));
            }
            BakedAnimations animations = null;
            if (animations().isPresent())
                animations = KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, animations().get(), JsonObject.class), "animations"), BakedAnimations.class);
            return new SuitLookData(Optional.ofNullable(model), texture, glowmaskPixels(), frames, glowmaskFrames(), Optional.ofNullable(animations));
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle clientbound sync suit look payload", e);
            return null;
        }
    }
}
