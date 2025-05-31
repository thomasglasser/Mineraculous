package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public record FlattenedMiraculousLookData(String look, Optional<String> model, byte[] pixels, Optional<byte[]> glowmaskPixels, Optional<String> transforms) {

    public static final StreamCodec<RegistryFriendlyByteBuf, FlattenedMiraculousLookData> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FlattenedMiraculousLookData::look,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedMiraculousLookData::model,
            ByteBufCodecs.BYTE_ARRAY, FlattenedMiraculousLookData::pixels,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), FlattenedMiraculousLookData::glowmaskPixels,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), FlattenedMiraculousLookData::transforms,
            FlattenedMiraculousLookData::new);
    public @Nullable MiraculousLookData unpack(ResourceKey<Miraculous> miraculous, Player target) {
        try {
            BakedGeoModel model = null;
            if (model().isPresent())
                model = BakedModelFactory.getForNamespace(Mineraculous.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, model().get(), JsonObject.class), Model.class)));
            ResourceLocation texture = Mineraculous.modLoc("textures/miraculouslooks/miraculouses/" + target.getStringUUID() + "/" + miraculous.location().getNamespace() + "/" + miraculous.location().getPath() + "/" + look() + ".png");
            MineraculousClientUtils.registerDynamicTexture(texture, pixels());
            ItemTransforms transforms = null;
            if (transforms().isPresent())
                transforms = BlockModel.fromString(transforms().get()).getTransforms();
            return new MiraculousLookData(Optional.ofNullable(model), texture, glowmaskPixels(), Optional.ofNullable(transforms));
        } catch (Exception e) {
            Mineraculous.LOGGER.error("Failed to handle clientbound sync miraculous look payload", e);
            return null;
        }
    }

    public static @Nullable FlattenedMiraculousLookData resolve(ResourceKey<Miraculous> miraculous, String look) throws IOException {
        Path folder = MineraculousClientUtils.getGameDirectory().resolve("miraculouslooks").resolve("miraculouses");
        if (!Files.exists(folder)) {
            return null;
        }
        String namespace = miraculous.location().getNamespace();
        Path namespaceFolder = folder.resolve(namespace);
        if (!Files.exists(namespaceFolder)) {
            return null;
        }
        String name = miraculous.location().getPath();
        Path nameFolder = namespaceFolder.resolve(name);
        if (!Files.exists(nameFolder)) {
            return null;
        }
        Path texture = folder.resolve(look + ".png");
        if (Files.exists(texture)) {
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
            return new FlattenedMiraculousLookData(look, Optional.ofNullable(convertedModel), convertedImage, Optional.ofNullable(convertedGlowmask), Optional.ofNullable(convertedDisplay));
        }
        return null;
    }
}
