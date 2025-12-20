package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds a collection of default look assets tied to a {@link LookAssetType}.
 */
public class DefaultLookAssets {
    private final ImmutableMap<LookAssetType<?>, Supplier<?>> assets;

    private DefaultLookAssets(ImmutableMap<LookAssetType<?>, Supplier<?>> assets) {
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
        return (T) assets.get(type).get();
    }

    public boolean hasAsset(LookAssetType<?> type) {
        return assets.containsKey(type);
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
        private final ImmutableMap.Builder<LookAssetType<?>, Supplier<?>> assets = new ImmutableMap.Builder<>();

        public <T> Builder add(LookAssetType<T> type, JsonElement asset) throws IOException, IllegalArgumentException {
            assets.put(type, type.loadDefault(asset));
            return this;
        }

        public DefaultLookAssets build() {
            return new DefaultLookAssets(assets.build());
        }
    }
}
