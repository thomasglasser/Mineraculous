package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Holds a collection of default look assets assigned to {@link LookAssetType}s.
 */
public class DefaultLookAssets {
    private final ImmutableMap<LookAssetType<?, ?>, Supplier<?>> assets;

    private DefaultLookAssets(ImmutableMap<LookAssetType<?, ?>, Supplier<?>> assets) {
        this.assets = assets;
    }

    /**
     * Retrieves the asset for the provided type.
     *
     * @param type The type to retrieve the asset for
     * @return The asset for the provided type
     * @param <T> The type of the asset
     */
    public <T> @Nullable T getAsset(LookAssetType<?, T> type) {
        Supplier<?> asset = assets.get(type);
        return asset != null ? (T) asset.get() : null;
    }

    /**
     * Checks if the provided asset type has an asset.
     * 
     * @param type The type to check
     * @return Whether the provided asset type has an asset
     */
    public boolean hasAsset(LookAssetType<?, ?> type) {
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
        private final ImmutableMap.Builder<LookAssetType<?, ?>, Supplier<?>> assets = new ImmutableMap.Builder<>();

        public <L> Builder add(LookAssetType<?, L> type, Supplier<L> asset) {
            assets.put(type, asset);
            return this;
        }

        public <S, L> Builder add(LookAssetType<S, L> type, JsonElement asset) throws IOException, IllegalArgumentException {
            return add(type, type.loadDefault(type.getCodec().parse(JsonOps.INSTANCE, asset).getOrThrow()));
        }

        public DefaultLookAssets build() {
            return new DefaultLookAssets(assets.build());
        }
    }
}
