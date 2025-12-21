package dev.thomasglasser.mineraculous.impl.client.look.asset;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.resources.ResourceLocation;

public class ScopeTextureLookAsset extends TextureLookAsset {
    public static final ScopeTextureLookAsset INSTANCE = new ScopeTextureLookAsset();
    private static final ResourceLocation KEY = MineraculousConstants.modLoc("scope_texture");

    private ScopeTextureLookAsset() {}

    @Override
    public ResourceLocation key() {
        return KEY;
    }
}
