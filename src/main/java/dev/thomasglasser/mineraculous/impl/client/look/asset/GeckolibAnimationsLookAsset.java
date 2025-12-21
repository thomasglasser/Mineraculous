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
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class GeckolibAnimationsLookAsset implements LookAssetType<String, BakedAnimations> {
    public static final GeckolibAnimationsLookAsset INSTANCE = new GeckolibAnimationsLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("geckolib_animations");

    private GeckolibAnimationsLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
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
    @Nullable
    public BakedAnimations load(String asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(JsonParser.parseString(Files.readString(LookManager.findValidPath(root, asset))).getAsJsonObject(), "animations"), BakedAnimations.class);
    }

    @Override
    public Supplier<BakedAnimations> loadDefault(String asset) {
        ResourceLocation location = ResourceLocation.parse(asset);
        return () -> GeckoLibCache.getBakedAnimations().get(location);
    }
}
