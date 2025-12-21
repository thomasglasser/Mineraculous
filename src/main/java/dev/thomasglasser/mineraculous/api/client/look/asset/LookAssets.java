package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds a collection of look assets tied to a {@link LookAssetType}.
 */
public class LookAssets {
    private final ImmutableMap<LookAssetType<?, ?>, ?> assets;

    private LookAssets(ImmutableMap<LookAssetType<?, ?>, ?> assets) {
        this.assets = assets;
    }

    /**
     * Retrieves the asset for the provided type.
     *
     * @param type The type to retrieve the asset for
     * @return The asset for the provided type
     * @param <L> The loaded type of the asset
     */
    public <L> L getAsset(LookAssetType<?, L> type) {
        return (L) assets.get(type);
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
        private final ImmutableMap.Builder<LookAssetType<?, ?>, Object> assets = new ImmutableMap.Builder<>();

        public <L> Builder add(LookAssetType<?, L> type, L asset) {
            assets.put(type, asset);
            return this;
        }

        public <S, L> Builder add(LookAssetType<S, L> type, JsonElement asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException {
            return add(type, type.load(type.getCodec().parse(JsonOps.INSTANCE, asset).getOrThrow(), root, hash, context));
        }

        public LookAssets build() {
            return new LookAssets(assets.build());
        }
    }
}
