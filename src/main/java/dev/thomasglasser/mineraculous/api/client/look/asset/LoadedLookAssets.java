package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds a collection of loaded look assets tied to a {@link LookAssetType}.
 */
public class LoadedLookAssets implements AbstractLookAssets {
    private final ImmutableMap<LookAssetType<?, ?>, ?> assets;

    private LoadedLookAssets(ImmutableMap<LookAssetType<?, ?>, ?> assets) {
        this.assets = assets;
    }

    /**
     * Retrieves the asset for the provided type.
     *
     * @param type The type to retrieve the asset for
     * @return The asset for the provided type
     * @param <L> The loaded type of the asset
     */
    public <L> L get(LookAssetType<?, L> type) {
        return (L) assets.get(type);
    }

    /**
     * Checks if the assets map contains an asset of the provided type.
     *
     * @param type The type to check
     * @return Whether the assets map contains an asset of the provided type
     */
    public boolean has(LookAssetType<?, ?> type) {
        return assets.containsKey(type);
    }

    /**
     * Checks if the assets map is empty.
     *
     * @return Whether the assets map is empty
     */
    public boolean isEmpty() {
        return assets.isEmpty();
    }

    @ApiStatus.Internal
    public static class Builder implements AbstractLookAssets.Builder<LoadedLookAssets> {
        private final ImmutableMap.Builder<LookAssetType<?, ?>, Object> assets = new ImmutableMap.Builder<>();
        private final Path root;

        public Builder(Path root) {
            this.root = root;
        }

        @Override
        public <S> AbstractLookAssets.Builder<LoadedLookAssets> add(LookAssetType<S, ?> type, JsonElement asset, ResourceLocation lookId, ResourceLocation contextId) throws IllegalArgumentException {
            try {
                assets.put(type, type.load(type.getCodec().parse(JsonOps.INSTANCE, asset).getOrThrow(), lookId, root, contextId));
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to load asset " + type.key() + " for look " + lookId + ": " + e.getMessage());
            }
            return this;
        }

        public LoadedLookAssets build() {
            return new LoadedLookAssets(assets.build());
        }
    }
}
