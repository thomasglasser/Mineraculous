package dev.thomasglasser.mineraculous.api.client.look;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.client.look.asset.AbstractLookAssets;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.look.metadata.LookMetadata;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataType;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A group of assets for {@link LookContext}s with a name and author.
 * 
 * @param name     The name of the look
 * @param author   The author of the look
 * @param metadata The metadata of the look
 * @param assets   The assets of the look
 * @param <T>      The type of the assets
 */
public record Look<T extends AbstractLookAssets>(String name, String author, LookMetadata metadata, ImmutableMap<ResourceKey<LookContext>, T> assets) {

    /// The JSON key for the name of the look.
    public static final String NAME_KEY = "name";
    /// The JSON key for the author of the look.
    public static final String AUTHOR_KEY = "author";
    /// The JSON key for the metadata of the look.
    public static final String METADATA_KEY = "metadata";
    /// The JSON key for the assets of the look.
    public static final String ASSETS_KEY = "assets";
    /**
     * Gets an asset for the provided context and asset type.
     * 
     * @param context   The context to use
     * @param assetType The asset type to use
     * @return The asset, or null if not found
     * @param <L> The loaded type of the asset
     */
    public <L> @Nullable L getAsset(ResourceKey<LookContext> context, LookAssetType<?, L> assetType) {
        T assets = this.assets.get(context);
        if (assets != null)
            return assets.get(assetType);
        return null;
    }

    public <L> @Nullable L getAsset(Holder<LookContext> context, LookAssetType<?, L> assetType) {
        return getAsset(context.getKey(), assetType);
    }

    @ApiStatus.Internal
    public static <T extends AbstractLookAssets> Look<T> load(JsonObject json, ResourceLocation lookId, Supplier<AbstractLookAssets.Builder<T>> builderSupplier) {
        if (!json.has(NAME_KEY))
            throw lookException(lookId, "missing " + NAME_KEY + " field");
        String name = json.get(NAME_KEY).getAsString();

        String author = json.has(AUTHOR_KEY) ? json.get(AUTHOR_KEY).getAsString() : "Unknown";

        LookMetadata.Builder metadataBuilder = new LookMetadata.Builder();
        if (json.has(METADATA_KEY)) {
            JsonObject metadata = json.getAsJsonObject(METADATA_KEY);
            for (String dataKey : metadata.keySet()) {
                LookMetadataType<?> type = MineraculousBuiltInRegistries.LOOK_METADATA_TYPE.get(ResourceLocation.parse(dataKey));
                if (type == null)
                    throw lookException(lookId, "has invalid metadata type " + dataKey);
                metadataBuilder.add(type, metadata.get(dataKey));
            }
        }

        if (!json.has(ASSETS_KEY)) throw lookException(lookId, "missing " + ASSETS_KEY + " field");
        JsonObject contexts = json.getAsJsonObject(ASSETS_KEY);

        Set<String> contextKeys = contexts.keySet();
        if (contextKeys.isEmpty()) throw lookException(lookId, "has no assets");

        ImmutableMap.Builder<ResourceKey<LookContext>, T> contextAssetsBuilder = new ImmutableMap.Builder<>();
        for (String contextKey : contextKeys) {
            ResourceLocation contextId = ResourceLocation.parse(contextKey);
            Holder<LookContext> context = MineraculousBuiltInRegistries.LOOK_CONTEXT.getHolder(contextId).orElseThrow(() -> lookException(lookId, "has invalid context " + contextId));
            if (context != null) {
                JsonObject assets = contexts.getAsJsonObject(contextKey);
                Set<String> assetKeys = assets.keySet();
                if (assetKeys.isEmpty()) throw lookException(lookId, "has no assets for context " + contextId);

                AbstractLookAssets.Builder<T> assetsBuilder = builderSupplier.get();
                for (String assetKey : assetKeys) {
                    ResourceLocation assetLoc = ResourceLocation.parse(assetKey);
                    if (!context.value().assetTypes().contains(assetLoc))
                        throw lookException(lookId, "has invalid asset type " + assetLoc + " for context " + contextId);
                    LookAssetType<?, ?> assetType = LookAssetTypes.get(assetLoc);
                    if (assetType == null)
                        throw lookException(lookId, "has invalid asset type " + assetLoc + " for context " + contextId);
                    assetsBuilder.add(assetType, assets.get(assetKey), lookId, contextId);
                }

                T lookAssets = assetsBuilder.build();
                if (lookAssets.isEmpty()) throw lookException(lookId, "has no assets for context " + contextId);

                contextAssetsBuilder.put(context.getKey(), lookAssets);
            }
        }

        ImmutableMap<ResourceKey<LookContext>, T> contextAssets = contextAssetsBuilder.build();

        if (contextAssets.isEmpty()) {
            throw lookException(lookId, "has no assets");
        }

        return new Look<>(name, author, metadataBuilder.build(), contextAssetsBuilder.build());
    }

    private static IllegalStateException lookException(ResourceLocation lookId, String message) {
        return new IllegalStateException("Look " + lookId + " " + message);
    }
}
