package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ItemTransformsLookAsset implements LookAssetType<ItemTransforms> {
    public static final ItemTransformsLookAsset INSTANCE = new ItemTransformsLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("item_transforms");
    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .create();

    private ItemTransformsLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }

    @Override
    public ItemTransforms load(Path path, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
        return GsonHelper.fromJson(DESERIALIZER, Files.readString(path), ItemTransforms.class);
    }
}
