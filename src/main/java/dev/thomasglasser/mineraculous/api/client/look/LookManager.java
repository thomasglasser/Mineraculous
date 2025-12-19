package dev.thomasglasser.mineraculous.api.client.look;

import com.google.common.collect.ImmutableSortedSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.InternalLookManager;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRequestLookPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    /**
     * Returns an immutable view of all equippable looks, sorted by name.
     * 
     * @return An immutable view of all equippable looks, sorted by name
     */
    public static ImmutableSortedSet<Look> getEquippable() {
        return InternalLookManager.getEquippable();
    }

    /**
     * Returns the equippable look for the provided hash.
     * 
     * @param hash The hash of the look
     * @return The equippable look for the provided hash, or null if not found
     */
    public static @Nullable Look getEquippableLook(String hash) {
        return InternalLookManager.getEquippableLook(hash);
    }

    /**
     * Returns the look of the player for the provided miraculous and context.
     *
     * @param player     The player to fetch the look for
     * @param miraculous The miraculous to fetch the look for
     * @param context    The context to fetch the look for
     * @return The look of the player for the provided miraculous and context, or null if not found
     */
    public static @Nullable Look getOrFetchLook(Player player, Holder<Miraculous> miraculous, ResourceKey<LookContext> context) {
        if (InternalLookManager.isInSafeMode()) {
            MineraculousConstants.LOGGER.warn("Tried to fetch look for {} in safe mode, using the default...", player.getUUID());
            return null;
        }

        String hash = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).lookData().hashes().get(context);
        if (hash == null) return null;

        Look look = InternalLookManager.getCachedLook(hash);
        if (look == null)
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestLookPayload(hash));
        return look;
    }

    /**
     * Returns the asset of the provided asset type of the look of the player for the provided miraculous and context.
     *
     * @param player     The player to fetch the asset for
     * @param miraculous The miraculous to fetch the asset for
     * @param context    The context to fetch the asset for
     * @param assetType  The asset type to fetch
     * @return The asset of the provided asset type of the look of the player for the provided miraculous and context, or null if not found
     * @param <T> The type of the asset
     */
    public static <T> @Nullable T getOrFetchLookAsset(Player player, Holder<Miraculous> miraculous, ResourceKey<LookContext> context, LookAssetType<T> assetType) {
        Look look = getOrFetchLook(player, miraculous, context);
        if (look != null) {
            LookAssets assets = look.assets().get(context);
            if (assets != null)
                return assets.getAsset(assetType);
        }
        return null;
    }

    public static <T> @Nullable T getOrFetchLookAsset(Player player, Holder<Miraculous> miraculous, Holder<LookContext> context, LookAssetType<T> assetType) {
        return getOrFetchLookAsset(player, miraculous, context.getKey(), assetType);
    }
}
