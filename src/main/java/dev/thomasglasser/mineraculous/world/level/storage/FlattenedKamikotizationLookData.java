package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public record FlattenedKamikotizationLookData(ResourceKey<Kamikotization> kamikotization, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, Optional<String> animations) {

    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedKamikotizationLookData> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), FlattenedKamikotizationLookData::kamikotization,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedKamikotizationLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedKamikotizationLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedKamikotizationLookData::glowmaskPixels,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedKamikotizationLookData::animations,
            FlattenedKamikotizationLookData::new);
    public KamikotizationLookData unpack(Player target) {
        try {
            BakedGeoModel model = null;
            if (model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/kamikotizations" + target.getStringUUID() + "/" + kamikotization().location().getNamespace() + "/" + kamikotization().location().getPath() + ".png");
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(NativeImage.read(pixels())));
            BakedAnimations animations = null;
            if (animations().isPresent())
                animations = KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, animations().get(), JsonObject.class), "animations"), BakedAnimations.class);
            return new KamikotizationLookData(Optional.ofNullable(model), texture, glowmaskPixels(), Optional.ofNullable(animations));
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle unpacking kamikotization look", e);
            return null;
        }
    }
}
