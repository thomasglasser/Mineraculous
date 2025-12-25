package dev.thomasglasser.mineraculous.api.client.look;

import dev.thomasglasser.mineraculous.api.client.look.asset.BuiltInLookAssets;
import dev.thomasglasser.mineraculous.api.client.look.asset.LoadedLookAssets;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.impl.client.look.ClientLookManager;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class LookManager {
    /**
     * Returns a set of all built-in look ids.
     * 
     * @return A set of all built-in look ids
     */
    public static Set<ResourceLocation> getBuiltIn() {
        return ClientLookManager.getBuiltIn();
    }

    /**
     * Gets a built-in look by its id.
     * 
     * @param id The id of the look
     * @return The built-in look, or null if not found
     */
    public static @Nullable Look<BuiltInLookAssets> getBuiltInLook(ResourceLocation id) {
        return ClientLookManager.getBuiltInLook(id);
    }

    /**
     * Gets a built-in look by its id,
     * or throws an exception if not found.
     *
     * @param id The id of the look
     * @return The built-in look
     */
    public static Look<BuiltInLookAssets> getOrThrowBuiltInLook(ResourceLocation id) {
        Look<BuiltInLookAssets> look = getBuiltInLook(id);
        if (look == null)
            throw new NullPointerException("No built-in look with id " + id + " found");
        return look;
    }

    /**
     * Returns a set of all equippable look hashes.
     * 
     * @return A set of all equippable look hashes
     */
    public static Set<String> getEquippable() {
        return ClientLookManager.getEquippable();
    }

    /**
     * Gets an equippable look by its hash.
     * 
     * @param hash The hash of the look
     * @return The equippable look, or null if not found
     */
    public static @Nullable Look<LoadedLookAssets> getEquippableLook(String hash) {
        return ClientLookManager.getEquippableLook(hash);
    }

    /**
     * Gets the appropriate look from the provided {@link LookData} for the provided {@link dev.thomasglasser.mineraculous.api.core.look.context.LookContext}.
     * 
     * @param data    The look data to use
     * @param context The context to use
     * @return The appropriate look, or null if not found
     */
    public static @Nullable Look<?> getLook(LookData data, ResourceKey<LookContext> context) {
        ResourceLocation lookId = data.looks().get(context);
        if (lookId != null) {
            if (!lookId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                return getBuiltInLook(lookId);
            return ClientLookManager.getLoadedLook(lookId.getPath());
        }
        return null;
    }

    public static @Nullable Look<?> getLook(LookData data, Holder<LookContext> context) {
        return getLook(data, context.getKey());
    }

    /**
     * Gets an asset from the provided {@link LookData} for the provided {@link dev.thomasglasser.mineraculous.api.core.look.context.LookContext} and {@link LookAssetType}.
     * 
     * @param data      The look data to use
     * @param context   The context to use
     * @param assetType The asset type to use
     * @return The asset, or null if not found
     * @param <L> The loaded type of the asset
     */
    public static <L> @Nullable L getAsset(LookData data, ResourceKey<LookContext> context, LookAssetType<?, L> assetType) {
        Look<?> look = getLook(data, context);
        if (look != null)
            return look.getAsset(context, assetType);
        return null;
    }

    public static <L> @Nullable L getAsset(LookData data, Holder<LookContext> context, LookAssetType<?, L> assetType) {
        return getAsset(data, context.getKey(), assetType);
    }
}
