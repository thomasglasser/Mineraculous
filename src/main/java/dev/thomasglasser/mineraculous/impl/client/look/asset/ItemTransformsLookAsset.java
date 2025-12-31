package dev.thomasglasser.mineraculous.impl.client.look.asset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ItemTransformsLookAsset implements LookAssetType<String, ItemTransforms> {
    public static final ItemTransformsLookAsset INSTANCE = new ItemTransformsLookAsset();
    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .create();

    private ItemTransformsLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.ITEM_TRANSFORMS;
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
    public ItemTransforms load(String asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException {
        return GsonHelper.fromJson(DESERIALIZER, Files.readString(LookUtils.findValidPath(root, asset)), ItemTransforms.class);
    }

    @Override
    public Supplier<ItemTransforms> getBuiltIn(String asset, ResourceLocation lookId) {
        ModelResourceLocation location = ModelResourceLocation.standalone(ResourceLocation.parse(asset).withPath(path -> path.substring("models/".length(), path.indexOf(".json"))));
        return () -> Minecraft.getInstance().getModelManager().getModel(location).getTransforms();
    }
}
