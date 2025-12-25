package dev.thomasglasser.mineraculous.impl.client.look.asset;

import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import net.minecraft.resources.ResourceLocation;

public class ScopeTextureLookAsset extends TextureLookAsset {
    public static final ScopeTextureLookAsset INSTANCE = new ScopeTextureLookAsset();

    private ScopeTextureLookAsset() {}

    @Override
    public ResourceLocation key() {
        return LookAssetTypeKeys.SCOPE_TEXTURE;
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
