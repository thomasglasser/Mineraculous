package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class CountdownTexturesLookAsset implements LookAssetType<CountdownTexturesLookAsset.CountdownTextures, ImmutableList<ResourceLocation>> {
    public static final CountdownTexturesLookAsset INSTANCE = new CountdownTexturesLookAsset();

    private CountdownTexturesLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.COUNTDOWN_TEXTURES;
    }

    @Override
    public Codec<CountdownTextures> getCodec() {
        return CountdownTextures.CODEC;
    }

    @Override
    public ImmutableList<ResourceLocation> load(CountdownTextures asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        ImmutableList.Builder<ResourceLocation> list = new ImmutableList.Builder<>();
        for (int i = 0; i < MiraculousData.COUNTDOWN_FRAMES; i++) {
            try {
                list.add(TextureLookAsset.load(LookManager.findValidPath(root, asset.base().replace(".png", "_" + i + ".png")), "textures/looks/" + hash + "_" + LookManager.toShortPath(context) + "_" + i));
            } catch (FileNotFoundException ignored) {}
        }
        return list.build();
    }

    @Override
    public Supplier<ImmutableList<ResourceLocation>> loadDefault(CountdownTextures asset) {
        ImmutableList.Builder<ResourceLocation> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < MiraculousData.COUNTDOWN_FRAMES; i++) {
            builder.add(ResourceLocation.parse(asset.base().replace(".png", "_" + i + ".png")));
        }
        ImmutableList<ResourceLocation> list = builder.build();
        return () -> list;
    }

    public record CountdownTextures(String base) {
        private static final Codec<CountdownTextures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base").forGetter(CountdownTextures::base)).apply(instance, CountdownTextures::new));
    }
}
