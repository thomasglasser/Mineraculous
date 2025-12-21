package dev.thomasglasser.mineraculous.api.client.look;

import com.google.common.collect.ImmutableSortedSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssets;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.impl.client.look.DefaultLook;
import dev.thomasglasser.mineraculous.impl.client.look.InternalLookManager;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRequestLookPayload;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    /**
     * Returns the default look for the provided key,
     * erroring if not found.
     * 
     * @param key The key of the look
     * @return The default look for the provided key
     */
    public static DefaultLook getDefaultLook(ResourceLocation key) {
        return InternalLookManager.getDefaultLook(key);
    }

    /**
     * Determines the default look id for a registry entry
     * 
     * @param key      The key of the entry
     * @param registry The registry of the entry
     * @return The default look id for the provided key and registry
     * @param <T> The type of the entry
     */
    public static <T> ResourceLocation getDefaultLookId(ResourceKey<T> key, ResourceKey<Registry<T>> registry) {
        ResourceLocation id = key.location();
        ResourceLocation registryId = registry.location();
        return id.withPrefix(registryId.getNamespace() + "/" + registryId.getPath() + "/");
    }

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
     * Returns the look of the player for the provided data and context.
     *
     * @param player   The player to fetch the look for
     * @param lookData The data to fetch the look from
     * @param context  The context to fetch the look for
     * @return The look of the player for the provided miraculous and context, or null if not found
     */
    public static @Nullable Look getOrFetchLook(Player player, LookData lookData, ResourceKey<LookContext> context) {
        if (InternalLookManager.isInSafeMode()) {
            MineraculousConstants.LOGGER.warn("Tried to fetch look for {} in safe mode, using the default...", player.getUUID());
            return null;
        }

        String hash = lookData.hashes().get(context);
        if (hash == null) return null;

        Look look = InternalLookManager.getCachedLook(hash);
        if (look == null)
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestLookPayload(hash));
        return look;
    }

    /**
     * Returns the asset of the provided asset type of the look of the player for the provided data and context.
     *
     * @param player    The player to fetch the asset for
     * @param lookData  The data to fetch the look from
     * @param context   The context to fetch the asset for
     * @param assetType The asset type to fetch
     * @return The asset of the provided asset type of the look of the player for the provided miraculous and context, or null if not found
     * @param <T> The type of the asset
     */
    public static <T> @Nullable T getOrFetchLookAsset(Player player, LookData lookData, ResourceKey<LookContext> context, LookAssetType<?, T> assetType) {
        Look look = getOrFetchLook(player, lookData, context);
        if (look != null) {
            LookAssets assets = look.assets().get(context);
            if (assets != null)
                return assets.getAsset(assetType);
        }
        return null;
    }

    public static <T> @Nullable T getOrFetchLookAsset(Player player, LookData lookData, Holder<LookContext> context, LookAssetType<?, T> assetType) {
        return getOrFetchLookAsset(player, lookData, context.getKey(), assetType);
    }

    /**
     * Finds the {@link Path} that the provided value is pointing to within the provided root.
     *
     * @param root  The root path of the pack
     * @param value The value to find within the root
     * @return The path of the provided value
     */
    public static Path findValidPath(Path root, String value) throws IOException {
        Path path = root.resolve(value);

        if (!path.normalize().startsWith(root.normalize())) {
            throw new IOException("Invalid path (Zip Slip attempt): " + value);
        }
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Referenced file not found: " + value);
        }
        if (Files.size(path) > ServerLookManager.MAX_FILE_SIZE)
            throw new IOException("File too large, must be <=2MB: " + value);

        return path;
    }
}
