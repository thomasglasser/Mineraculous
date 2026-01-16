package dev.thomasglasser.mineraculous.impl.core.look.context.miraculous;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import net.minecraft.resources.ResourceLocation;

public class MiraculousSuitLookContext extends TransformedMiraculousLookContext {
    private static final ImmutableSet<ResourceLocation> ASSET_TYPES = ImmutableSet.of(
            LookAssetTypeKeys.TEXTURE,
            LookAssetTypeKeys.GECKOLIB_MODEL,
            LookAssetTypeKeys.GECKOLIB_ANIMATIONS,
            LookAssetTypeKeys.TRANSFORMATION_TEXTURES);

    @Override
    public ImmutableSet<ResourceLocation> assetTypes() {
        return ASSET_TYPES;
    }
}
