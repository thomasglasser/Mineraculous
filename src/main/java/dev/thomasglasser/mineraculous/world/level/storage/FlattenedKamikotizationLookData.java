package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public record FlattenedKamikotizationLookData(Holder<Kamikotization> kamikotization, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, Optional<String> animations) {

    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedKamikotizationLookData> CODEC = StreamCodec.composite(
            Kamikotization.STREAM_CODEC, FlattenedKamikotizationLookData::kamikotization,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedKamikotizationLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedKamikotizationLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedKamikotizationLookData::glowmaskPixels,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedKamikotizationLookData::animations,
            FlattenedKamikotizationLookData::new);
    public @Nullable KamikotizationLookData unpack(Player target) {
        try {
            BakedGeoModel model = null;
            if (model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/kamikotizations" + target.getStringUUID() + "/" + kamikotization().getKey().location().getNamespace() + "/" + kamikotization().getKey().location().getPath() + ".png");
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

    public static @Nullable FlattenedKamikotizationLookData resolve(Holder<Kamikotization> kamikotization) throws IOException {
        Path folder = MineraculousClientUtils.getGameDirectory().resolve("miraculouslooks").resolve("kamikotizations");
        if (!Files.exists(folder)) {
            return null;
        }
        String namespace = kamikotization.getKey().location().getNamespace();
        Path namespaceFolder = folder.resolve(namespace);
        if (!Files.exists(namespaceFolder)) {
            return null;
        }
        String name = kamikotization.getKey().location().getPath();
        Path texture = namespaceFolder.resolve(name + ".png");
        if (Files.exists(texture)) {
            Path model = texture.resolveSibling(name + ".geo.json");
            String convertedModel = null;
            if (Files.exists(model)) {
                convertedModel = Files.readString(model);
            }
            byte[] convertedImage = NativeImage.read(texture.toUri().toURL().openStream()).asByteArray();
            Path glowmask = texture.resolveSibling(name + "_glowmask.png");
            byte[] convertedGlowmask = null;
            if (Files.exists(glowmask)) {
                convertedGlowmask = NativeImage.read(glowmask.toUri().toURL().openStream()).asByteArray();
            }
            Path animations = texture.resolveSibling(name + ".animation.json");
            String convertedAnimations = null;
            if (Files.exists(animations)) {
                convertedAnimations = Files.readString(animations);
            }
            return new FlattenedKamikotizationLookData(kamikotization, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedAnimations));
        }
        return null;
    }
}
