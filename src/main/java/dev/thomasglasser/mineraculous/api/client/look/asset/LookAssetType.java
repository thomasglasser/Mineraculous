package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a type of asset that can be loaded and used in a {@link dev.thomasglasser.mineraculous.api.core.look.context.LookContext}.
 *
 * @param <S> The stored type of the asset
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
     * Returns the codec to de/serialize the stored asset type.
     * 
     * @return The codec to de/serialize the stored asset type
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
     * @param asset   The asset to load
     * @param root    The root path of the look
     * @param hash    The hash of the look (for ID purposes)
     * @param context The {@link dev.thomasglasser.mineraculous.api.core.look.context.LookContext} to load the asset for
     * @return The loaded asset
     */
    L load(S asset, Path root, String hash, ResourceLocation context) throws IOException, IllegalArgumentException;

    /**
     * Loads the default asset.
     *
     * @param asset The asset to load
     * @return A supplier pointing to the loaded asset
     */
    Supplier<L> loadDefault(S asset);
}
