package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class GeckolibAnimationsLookAsset implements LookAssetType<BakedAnimations> {
    public static final GeckolibAnimationsLookAsset INSTANCE = new GeckolibAnimationsLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("geckolib_animations");

    private GeckolibAnimationsLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public BakedAnimations load(JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(JsonParser.parseString(Files.readString(LookManager.findValidPath(root, asset.getAsString()))).getAsJsonObject(), "animations"), BakedAnimations.class);
    }
}
