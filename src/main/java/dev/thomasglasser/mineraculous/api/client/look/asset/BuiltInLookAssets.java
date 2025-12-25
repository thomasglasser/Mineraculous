package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Holds a collection of built-in look assets assigned to {@link LookAssetType}s.
 */
public class BuiltInLookAssets implements AbstractLookAssets {
    private final ImmutableMap<LookAssetType<?, ?>, Supplier<?>> assets;

    private BuiltInLookAssets(ImmutableMap<LookAssetType<?, ?>, Supplier<?>> assets) {
        this.assets = assets;
    }

    /**
     * Retrieves the asset for the provided type.
     *
     * @param type The type to retrieve the asset for
     * @return The asset for the provided type
     * @param <L> The loaded type of the asset
     */
    public <L> @Nullable L get(LookAssetType<?, L> type) {
        Supplier<?> asset = assets.get(type);
        return asset != null ? (L) asset.get() : null;
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
    public static class Builder implements AbstractLookAssets.Builder<BuiltInLookAssets> {
        private final ImmutableMap.Builder<LookAssetType<?, ?>, Supplier<?>> assets = new ImmutableMap.Builder<>();

        @Override
        public <S> Builder add(LookAssetType<S, ?> type, JsonElement asset, ResourceLocation lookId, ResourceLocation contextId) throws IllegalArgumentException {
            assets.put(type, type.getBuiltIn(type.getCodec().parse(JsonOps.INSTANCE, asset).getOrThrow(), lookId));
            return this;
        }

        @Override
        public BuiltInLookAssets build() {
            return new BuiltInLookAssets(assets.build());
        }
    }
}
