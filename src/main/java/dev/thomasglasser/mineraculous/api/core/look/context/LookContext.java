package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a context of a look with certain used {@link dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType}s.
 *
 * @param assetTypes The keys of the {@link dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType}s used by this context
 */
public record LookContext(ImmutableSet<ResourceLocation> assetTypes) {}
