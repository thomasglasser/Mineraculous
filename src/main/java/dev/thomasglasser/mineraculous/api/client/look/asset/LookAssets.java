package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds a collection of look assets tied to a {@link LookAssetType}.
 */
public class LookAssets {
    private final ImmutableMap<LookAssetType<?>, ?> assets;

    private LookAssets(ImmutableMap<LookAssetType<?>, ?> assets) {
        this.assets = assets;
    }

    /**
     * Retrieves the asset for the provided type.
     *
     * @param type The type to retrieve the asset for
     * @return The asset for the provided type
     * @param <T> The type of the asset
     */
    public <T> T getAsset(LookAssetType<T> type) {
        return (T) assets.get(type);
    }

    /**
     * Checks if the provided assets map is empty.
     *
     * @return True if the assets map is empty, false otherwise
     */
    public boolean isEmpty() {
        return assets.isEmpty();
    }

    @ApiStatus.Internal
    public static class Builder {
        private final ImmutableMap.Builder<LookAssetType<?>, Object> assets = new ImmutableMap.Builder<>();

        public <T> Builder add(LookAssetType<T> type, JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
            assets.put(type, type.load(asset, root, hash, context));
            return this;
        }

        public LookAssets build() {
            return new LookAssets(assets.build());
        }
    }
}
