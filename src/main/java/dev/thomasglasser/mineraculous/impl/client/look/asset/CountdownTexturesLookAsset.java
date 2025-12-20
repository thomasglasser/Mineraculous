package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class CountdownTexturesLookAsset implements LookAssetType<ImmutableList<ResourceLocation>> {
    public static final CountdownTexturesLookAsset INSTANCE = new CountdownTexturesLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("countdown_textures");
    private static final String BASE_KEY = "base";

    private CountdownTexturesLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public ImmutableList<ResourceLocation> load(JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        ImmutableList.Builder<ResourceLocation> list = new ImmutableList.Builder<>();
        JsonObject assetObject = asset.getAsJsonObject();
        String base = assetObject.get(BASE_KEY).getAsString();
        for (int i = 1; i < MiraculousData.COUNTDOWN_FRAMES; i++) {
            try {
                list.add(TextureLookAsset.load(LookManager.findValidPath(root, base.replace(".png", "_" + i + ".png")), "textures/looks/" + hash + "_" + context.getNamespace() + "_" + context.getPath() + "_" + i));
            } catch (FileNotFoundException ignored) {}
        }
        return list.build();
    }

    @Override
    public Supplier<ImmutableList<ResourceLocation>> loadDefault(JsonElement asset) {
        ImmutableList.Builder<ResourceLocation> builder = new ImmutableList.Builder<>();
        JsonObject assetObject = asset.getAsJsonObject();
        String base = assetObject.get(BASE_KEY).getAsString();
        for (int i = 1; i < MiraculousData.COUNTDOWN_FRAMES; i++) {
            builder.add(ResourceLocation.parse(base.replace(".png", "_" + i + ".png")));
        }
        ImmutableList<ResourceLocation> list = builder.build();
        return () -> list;
    }
}
