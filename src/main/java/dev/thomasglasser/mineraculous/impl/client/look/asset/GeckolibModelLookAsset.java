package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
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
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("geckolib_model");

    private GeckolibModelLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public BakedGeoModel load(String asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return BakedModelFactory.getForNamespace(MineraculousConstants.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(JsonParser.parseString(Files.readString(LookManager.findValidPath(root, asset))).getAsJsonObject(), Model.class)));
    }

    @Override
    public Supplier<BakedGeoModel> loadDefault(String asset) {
        ResourceLocation location = ResourceLocation.parse(asset);
        return () -> GeckoLibCache.getBakedModels().get(location);
    }
}
