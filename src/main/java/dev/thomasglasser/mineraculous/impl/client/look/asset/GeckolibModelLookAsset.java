package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

public class GeckolibModelLookAsset implements LookAssetType<String, BakedGeoModel> {
    public static final GeckolibModelLookAsset INSTANCE = new GeckolibModelLookAsset();

    private GeckolibModelLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.GECKOLIB_MODEL;
    }

    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public BakedGeoModel load(String asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException {
        return BakedModelFactory.getForNamespace(MineraculousConstants.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(JsonParser.parseString(Files.readString(LookUtils.findValidPath(root, asset))).getAsJsonObject(), Model.class)));
    }

    @Override
    public Supplier<BakedGeoModel> getBuiltIn(String asset, ResourceLocation lookId) {
        ResourceLocation location = ResourceLocation.parse(asset);
        return () -> GeckoLibCache.getBakedModels().get(location);
    }
}
