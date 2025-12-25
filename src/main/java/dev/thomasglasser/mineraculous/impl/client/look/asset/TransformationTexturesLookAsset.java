package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class TransformationTexturesLookAsset implements LookAssetType<TransformationTexturesLookAsset.TransformationTextures, Int2ObjectMap<ResourceLocation>> {
    public static final TransformationTexturesLookAsset INSTANCE = new TransformationTexturesLookAsset();

    private TransformationTexturesLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.TRANSFORMATION_TEXTURES;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public Codec<TransformationTextures> getCodec() {
        return TransformationTextures.CODEC;
    }

    @Override
    public Int2ObjectMap<ResourceLocation> load(TransformationTextures asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException {
        Int2ObjectMap<ResourceLocation> map = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < asset.textureCount(); i++) {
            try {
                map.put(i, TextureLookAsset.load(LookUtils.findValidPath(root, asset.base().replace(".png", "_" + i + ".png")), lookId, key(), contextId, "_" + i));
            } catch (FileNotFoundException ignored) {}
        }
        return Int2ObjectMaps.unmodifiable(map);
    }

    @Override
    public Supplier<Int2ObjectMap<ResourceLocation>> getBuiltIn(TransformationTextures asset, ResourceLocation lookId) {
        Int2ObjectMap<ResourceLocation> map = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < asset.textureCount(); i++) {
            map.put(i, ResourceLocation.parse(asset.base().replace(".png", "_" + i + ".png")));
        }
        Int2ObjectMap<ResourceLocation> immutable = Int2ObjectMaps.unmodifiable(map);
        return () -> immutable;
    }

    public record TransformationTextures(String base, int textureCount) {
        private static final Codec<TransformationTextures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base").forGetter(TransformationTextures::base),
                ExtraCodecs.POSITIVE_INT.fieldOf("texture_count").forGetter(TransformationTextures::textureCount)).apply(instance, TransformationTextures::new));
    }
}
