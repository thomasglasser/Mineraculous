package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class GeckolibAnimationsLookAsset implements LookAssetType<String, BakedAnimations> {
    public static final GeckolibAnimationsLookAsset INSTANCE = new GeckolibAnimationsLookAsset();

    private GeckolibAnimationsLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.GECKOLIB_ANIMATIONS;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public BakedAnimations load(String asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException {
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(JsonParser.parseString(Files.readString(LookUtils.findValidPath(root, asset))).getAsJsonObject(), "animations"), BakedAnimations.class);
    }

    @Override
    public Supplier<BakedAnimations> getBuiltIn(String asset, ResourceLocation lookId) {
        ResourceLocation location = ResourceLocation.parse(asset);
        return () -> GeckoLibCache.getBakedAnimations().get(location);
    }
}
