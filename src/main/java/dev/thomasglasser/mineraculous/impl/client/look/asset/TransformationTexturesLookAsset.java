package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
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
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("transformation_textures");

    private TransformationTexturesLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
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
    public Int2ObjectMap<ResourceLocation> load(TransformationTextures asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        Int2ObjectMap<ResourceLocation> map = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < asset.textureCount(); i++) {
            try {
                map.put(i, TextureLookAsset.load(LookManager.findValidPath(root, asset.base().replace(".png", "_" + i + ".png")), "textures/looks/" + hash + "_" + context.getNamespace() + "_" + context.getPath() + "_" + i));
            } catch (FileNotFoundException ignored) {}
        }
        return Int2ObjectMaps.unmodifiable(map);
    }

    @Override
    public Supplier<Int2ObjectMap<ResourceLocation>> loadDefault(TransformationTextures asset) {
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
