package dev.thomasglasser.mineraculous.impl.client.look;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.DefaultLookAssets;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record DefaultLook(ImmutableMap<ResourceKey<LookContext>, DefaultLookAssets> assets) {
    public <T> @Nullable T getAsset(ResourceKey<LookContext> context, LookAssetType<?, T> assetType) {
        DefaultLookAssets assets = this.assets.get(context);
        if (assets != null)
            return assets.getAsset(assetType);
        return null;
    }

    public <T> @Nullable T getAsset(Holder<LookContext> context, LookAssetType<?, T> assetType) {
        return getAsset(context.getKey(), assetType);
    }

    public static @Nullable DefaultLook load(JsonObject json, ResourceLocation location) {
        try {
            Set<String> contextKeys = json.keySet();
            if (contextKeys.isEmpty()) throw new IllegalArgumentException("Look " + location + " has no assets");

            ImmutableMap.Builder<ResourceKey<LookContext>, DefaultLookAssets> contextAssetsBuilder = new ImmutableMap.Builder<>();
            for (String contextKey : contextKeys) {
                ResourceLocation contextLoc = ResourceLocation.parse(contextKey);
                Holder<LookContext> context = MineraculousBuiltInRegistries.LOOK_CONTEXT.getHolder(contextLoc).orElse(null);
                if (context != null) {
                    JsonObject assets = json.getAsJsonObject(contextKey);
                    Set<String> assetKeys = assets.keySet();
                    if (assetKeys.isEmpty()) throw new IllegalArgumentException("Look " + location + " has no assets for context " + contextLoc);

                    DefaultLookAssets.Builder assetsBuilder = new DefaultLookAssets.Builder();
                    for (String assetKey : assetKeys) {
                        ResourceLocation assetLoc = ResourceLocation.parse(assetKey);
                        if (!context.value().assetTypes().contains(assetLoc))
                            throw new IllegalArgumentException("Asset type " + assetKey + " not valid for context " + contextLoc);
                        LookAssetType<?, ?> assetType = LookAssetTypes.get(assetLoc);
                        if (assetType == null)
                            throw new IllegalArgumentException("Invalid asset type " + assetKey + " for context " + contextLoc);
                        assetsBuilder.add(assetType, assets.get(assetKey));
                    }

                    DefaultLookAssets lookAssets = assetsBuilder.build();
                    if (lookAssets.isEmpty()) throw new IllegalArgumentException("Look " + lookAssets + " has no assets for context " + contextLoc);
                    for (ResourceLocation key : context.value().assetTypes()) {
                        LookAssetType<?, ?> assetType = LookAssetTypes.get(key);
                        if (assetType == null)
                            throw new IllegalArgumentException("Look " + location + " has invalid asset type " + key + " for context " + contextLoc);
                        if (!assetType.isOptional() && !lookAssets.hasAsset(assetType))
                            throw new IllegalArgumentException("Look " + location + " has no asset for type " + key + " for context " + contextLoc);
                    }

                    contextAssetsBuilder.put(context.getKey(), lookAssets);
                }
            }

            ImmutableMap<ResourceKey<LookContext>, DefaultLookAssets> contextAssets = contextAssetsBuilder.build();

            if (contextAssets.isEmpty()) {
                throw new IllegalArgumentException("Look " + location + " has no assets");
            }

            return new DefaultLook(contextAssets);
        } catch (Exception e) {
            MineraculousConstants.LOGGER.error("Failed to load default look {}: {}", location, e.getMessage());
            return null;
        }
    }
}
