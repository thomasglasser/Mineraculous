package dev.thomasglasser.mineraculous.api.client.look;

import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

public interface LookRenderer {
    Holder<LookContext> getCurrentLookContext();

    @Nullable
    Look getCurrentLook();

    default <T> @Nullable T getAsset(LookAssetType<T> assetType, Holder<LookContext> context) {
        Look look = getCurrentLook();
        if (look != null) {
            LookAssets assets = look.assets().get(context.getKey());
            if (assets != null)
                return assets.getAsset(assetType);
        }
        return null;
    }

    default <T> @Nullable T getAsset(LookAssetType<T> assetType) {
        return getAsset(assetType, getCurrentLookContext());
    }

    default <T> T getAssetOrDefault(LookAssetType<T> assetType, Holder<LookContext> context, Supplier<T> fallback) {
        T asset = getAsset(assetType, context);
        return asset != null ? asset : fallback.get();
    }

    default <T> T getAssetOrDefault(LookAssetType<T> assetType, Supplier<T> fallback) {
        return getAssetOrDefault(assetType, getCurrentLookContext(), fallback);
    }
}
