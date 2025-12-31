package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/// Represents a context of a look with {@link dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType}s.
public interface LookContext {
    /**
     * Returns the set of IDs for {@link dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType}s used.
     * 
     * @return The set of IDs
     */
    ImmutableSet<ResourceLocation> assetTypes();

    /**
     * Prepares the provided player for previewing the context with a look.
     * 
     * @param player   The player to prepare
     * @param selected The selected element
     */
    void preparePreview(Player player, Holder<?> selected);
}
