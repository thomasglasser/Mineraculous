package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;

/**
 * Represents a context of a look with certain used {@link LookAssetType}s.
 *
 * @param assetTypes The {@link LookAssetType}s used by this context
 */
public record LookContext(ImmutableSet<LookAssetType<?>> assetTypes) {}
