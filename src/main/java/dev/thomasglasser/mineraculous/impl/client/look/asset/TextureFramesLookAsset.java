package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;

public class TextureFramesLookAsset implements LookAssetType<Int2ObjectMap<ResourceLocation>> {
    public static final TextureFramesLookAsset INSTANCE = new TextureFramesLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("texture_frames");
    private static final String BASE_KEY = "base";
    private static final String FRAMES_KEY = "frames";

    private TextureFramesLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public Int2ObjectMap<ResourceLocation> load(JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        Int2ObjectMap<ResourceLocation> map = new Int2ObjectOpenHashMap<>();
        JsonObject assetObject = asset.getAsJsonObject();
        String base = assetObject.get(BASE_KEY).getAsString();
        int frames = assetObject.get(FRAMES_KEY).getAsInt();
        for (int i = 0; i < frames; i++) {
            try {
                map.put(i, TextureLookAsset.load(LookManager.findValidPath(root, base.replace(".png", "_" + i + ".png")), "textures/looks/" + hash + "_" + context.getNamespace() + "_" + context.getPath() + "_" + i));
            } catch (FileNotFoundException ignored) {}
        }
        return Int2ObjectMaps.unmodifiable(map);
    }
}
