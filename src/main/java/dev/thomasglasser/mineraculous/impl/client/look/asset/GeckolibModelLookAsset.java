package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

public class GeckolibModelLookAsset implements LookAssetType<BakedGeoModel> {
    public static final GeckolibModelLookAsset INSTANCE = new GeckolibModelLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("geckolib_model");

    private GeckolibModelLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public BakedGeoModel load(JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return BakedModelFactory.getForNamespace(MineraculousConstants.MOD_ID).constructGeoModel(GeometryTree.fromModel(KeyFramesAdapter.GEO_GSON.fromJson(JsonParser.parseString(Files.readString(LookManager.findValidPath(root, asset.getAsString()))).getAsJsonObject(), Model.class)));
    }

    @Override
    public Supplier<BakedGeoModel> loadDefault(JsonElement asset) {
        ResourceLocation location = ResourceLocation.parse(asset.getAsString());
        return () -> GeckoLibCache.getBakedModels().get(location);
    }
}
