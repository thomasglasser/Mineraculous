package dev.thomasglasser.mineraculous.api.core.look.asset;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds the keys for included {@link dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType}s,
 * separated for side safety.
 */
public class LookAssetTypeKeys {
    // General
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#TEXTURE
    public static final ResourceLocation TEXTURE = create("texture");
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#GECKOLIB_MODEL
    public static final ResourceLocation GECKOLIB_MODEL = create("geckolib_model");
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#GECKOLIB_ANIMATIONS
    public static final ResourceLocation GECKOLIB_ANIMATIONS = create("geckolib_animations");
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#ITEM_TRANSFORMS
    public static final ResourceLocation ITEM_TRANSFORMS = create("item_transforms");

    // Specific
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#TRANSFORMATION_TEXTURES
    public static final ResourceLocation TRANSFORMATION_TEXTURES = create("transformation_textures");
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#COUNTDOWN_TEXTURES
    public static final ResourceLocation COUNTDOWN_TEXTURES = create("countdown_textures");
    /// @see dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes#SCOPE_TEXTURE
    public static final ResourceLocation SCOPE_TEXTURE = create("scope_texture");

    private static ResourceLocation create(String name) {
        return MineraculousConstants.modLoc(name);
    }
}
