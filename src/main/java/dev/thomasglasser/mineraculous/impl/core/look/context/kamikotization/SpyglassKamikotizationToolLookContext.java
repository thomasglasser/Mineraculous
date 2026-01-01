package dev.thomasglasser.mineraculous.impl.core.look.context.kamikotization;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import net.minecraft.resources.ResourceLocation;

public class SpyglassKamikotizationToolLookContext extends KamikotizationLookContext {
    private static final ImmutableSet<ResourceLocation> ASSET_TYPES = ImmutableSet.of(
            LookAssetTypeKeys.TEXTURE,
            LookAssetTypeKeys.GECKOLIB_MODEL,
            LookAssetTypeKeys.GECKOLIB_ANIMATIONS,
            LookAssetTypeKeys.SCOPE_TEXTURE);

    @Override
    public ImmutableSet<ResourceLocation> assetTypes() {
        return ASSET_TYPES;
    }
}
