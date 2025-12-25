package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a type of asset that can be de/serialized, loaded, and used in a {@link dev.thomasglasser.mineraculous.api.core.look.context.LookContext}.
 *
 * @param <S> The serialization type of the asset
 * @param <L> The loaded type of the asset
 */
public interface LookAssetType<S, L> {
    Codec<LookAssetType<?, ?>> CODEC = ResourceLocation.CODEC
            .comapFlatMap(
                    id -> Optional.ofNullable(LookAssetTypes.get(id))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown look asset type: " + id)),
                    LookAssetType::key);

    /**
     * Returns the serialization key of the asset type
     * 
     * @return The serialization key of the asset type
     */
    ResourceLocation key();

    /**
     * Returns the codec to de/serialize the asset type.
     * 
     * @return The codec to de/serialize the asset type
     */
    Codec<S> getCodec();

    /**
     * Returns whether the asset is optional.
     * 
     * @return Whether the asset is optional
     */
    default boolean isOptional() {
        return false;
    }

    /**
     * Loads the asset from the provided path.
     *
     * @param asset     The asset to load
     * @param lookId    The id of the look
     * @param root      The root path of the look
     * @param contextId The context of the asset
     * @return The loaded asset
     */
    L load(S asset, ResourceLocation lookId, Path root, ResourceLocation contextId) throws IOException, IllegalArgumentException;

    /**
     * Gets the built-in asset.
     *
     * @param asset  The asset to get
     * @param lookId The id of the look
     * @return A supplier pointing to the asset
     */
    Supplier<L> getBuiltIn(S asset, ResourceLocation lookId);
}
