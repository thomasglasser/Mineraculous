package dev.thomasglasser.mineraculous.api.client.look;

import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.impl.client.look.DefaultLook;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/// Represents a renderer that uses the look system.
public interface LookRenderer {
    /**
     * Returns the ID of the renderer's default look.
     * 
     * @return The ID of the renderer's default look
     */
    ResourceLocation getDefaultLookId();

    /**
     * Returns the look context of the renderer.
     * 
     * @return The look context of the renderer
     */
    Holder<LookContext> getContext();

    /**
     * Returns the look of the renderer.
     * 
     * @return The look of the renderer
     */
    @Nullable
    Look getLook();

    /**
     * Returns the default look of the renderer.
     * 
     * @return The default look of the renderer
     */
    default DefaultLook getDefaultLook() {
        return LookManager.getDefaultLook(getDefaultLookId());
    }

    /**
     * Returns the default asset of the provided type for the provided context.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @param context   The context for the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> T getDefaultAsset(LookAssetType<T> assetType, Holder<LookContext> context) {
        return getDefaultLook().getAsset(assetType, context);
    }

    /**
     * Returns the default asset of the provided type for the current context.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> T getDefaultAsset(LookAssetType<T> assetType) {
        return getDefaultAsset(assetType, getContext());
    }

    /**
     * Returns the asset of the provided type for the provided context from the current look,
     * or null if no look is present.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @param context   The context for the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> @Nullable T getAsset(LookAssetType<T> assetType, Holder<LookContext> context) {
        Look look = getLook();
        if (look != null) {
            LookAssets assets = look.assets().get(context.getKey());
            if (assets != null)
                return assets.getAsset(assetType);
        }
        return null;
    }

    /**
     * Returns the asset of the provided type for the current context from the current look,
     * or null if no look is present.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> @Nullable T getAsset(LookAssetType<T> assetType) {
        return getAsset(assetType, getContext());
    }

    /**
     * Returns the asset of the provided type for the provided context from the current look,
     * or the default look if no look is present.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @param context   The context for the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> T getAssetOrDefault(LookAssetType<T> assetType, Holder<LookContext> context) {
        T asset = getAsset(assetType, context);
        if (asset != null)
            return asset;
        return getDefaultAsset(assetType, context);
    }

    /**
     * Returns the asset of the provided type for the current context from the current look,
     * or the default look if no look is present.
     * 
     * @param assetType The {@link LookAssetType} of the asset
     * @return The asset
     * @param <T> The type of the asset
     */
    default <T> T getAssetOrDefault(LookAssetType<T> assetType) {
        return getAssetOrDefault(assetType, getContext());
    }
}
