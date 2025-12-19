package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.impl.client.look.asset.GeckolibAnimationsLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.GeckolibModelLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.ItemTransformsLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TextureLookAsset;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class LookAssetTypes {
    /// Holds all known {@link LookAssetType}s.
    private static final Set<LookAssetType<?>> REGISTRY = new ReferenceOpenHashSet<>();

    /// Represents the {@link ResourceLocation} of a texture in the {@link net.minecraft.client.renderer.texture.TextureManager}.
    public static final LookAssetType<ResourceLocation> TEXTURE = register(TextureLookAsset.INSTANCE);
    /// Represents a {@link BakedGeoModel} used in {@link software.bernie.geckolib.model.GeoModel}s.
    public static final LookAssetType<BakedGeoModel> GECKOLIB_MODEL = register(GeckolibModelLookAsset.INSTANCE);
    /// Represents a {@link BakedAnimations} used in {@link software.bernie.geckolib.model.GeoModel}s.
    public static final LookAssetType<BakedAnimations> GECKOLIB_ANIMATIONS = register(GeckolibAnimationsLookAsset.INSTANCE);
    /// Represents the {@link ItemTransforms} of an item to alter rendering placement.
    public static final LookAssetType<ItemTransforms> ITEM_TRANSFORMS = register(ItemTransformsLookAsset.INSTANCE);

    /**
     * Returns an immutable view of all available {@link LookAssetType}s.
     *
     * @return An immutable view of all available {@link LookAssetType}s
     */
    public static Set<LookAssetType<?>> values() {
        return ImmutableSet.copyOf(REGISTRY);
    }

    /**
     * Finds the {@link LookAssetType} for the provided key.
     *
     * @param key The key of the {@link LookAssetType}
     * @return The {@link LookAssetType} for the provided key, or null if not found
     */
    public static @Nullable LookAssetType<?> get(ResourceLocation key) {
        for (LookAssetType<?> type : REGISTRY) {
            if (type.key().equals(key))
                return type;
        }
        return null;
    }

    /**
     * Registers a {@link LookAssetType} to the registry.
     *
     * @param type The {@link LookAssetType} to register
     * @return The registered {@link LookAssetType}
     * @param <T> The type of asset
     */
    public static <T> LookAssetType<T> register(LookAssetType<T> type) {
        REGISTRY.add(type);
        return type;
    }
}
