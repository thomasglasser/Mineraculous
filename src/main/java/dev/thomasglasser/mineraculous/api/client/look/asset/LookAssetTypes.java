package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.impl.client.look.asset.CountdownTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.GeckolibAnimationsLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.GeckolibModelLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.ItemTransformsLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.ScopeTextureLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TextureLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TransformationTexturesLookAsset;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class LookAssetTypes {
    /// Holds all known {@link LookAssetType}s.
    private static final Map<ResourceLocation, LookAssetType<?, ?>> REGISTRY = new Object2ObjectOpenHashMap<>();

    // General
    /// The {@link ResourceLocation} of a texture in the {@link net.minecraft.client.renderer.texture.TextureManager}.
    public static final LookAssetType<String, ResourceLocation> TEXTURE = register(TextureLookAsset.INSTANCE);
    /// A {@link BakedGeoModel} used in {@link software.bernie.geckolib.model.GeoModel}s.
    public static final LookAssetType<String, BakedGeoModel> GECKOLIB_MODEL = register(GeckolibModelLookAsset.INSTANCE);
    /// An optional {@link BakedAnimations} used in {@link software.bernie.geckolib.model.GeoModel}s.
    public static final LookAssetType<String, BakedAnimations> GECKOLIB_ANIMATIONS = register(GeckolibAnimationsLookAsset.INSTANCE);
    /// The optional {@link ItemTransforms} of an item to alter rendering placement.
    public static final LookAssetType<String, ItemTransforms> ITEM_TRANSFORMS = register(ItemTransformsLookAsset.INSTANCE);

    // Specific
    /// An optional map of transformation frames to {@link ResourceLocation}s in the {@link net.minecraft.client.renderer.texture.TextureManager}.
    public static final LookAssetType<TransformationTexturesLookAsset.TransformationTextures, Int2ObjectMap<ResourceLocation>> TRANSFORMATION_TEXTURES = register(TransformationTexturesLookAsset.INSTANCE);
    /// A list of countdown texture {@link ResourceLocation}s in the {@link net.minecraft.client.renderer.texture.TextureManager}.
    public static final LookAssetType<CountdownTexturesLookAsset.CountdownTextures, ImmutableList<ResourceLocation>> COUNTDOWN_TEXTURES = register(CountdownTexturesLookAsset.INSTANCE);
    /// The optional {@link ResourceLocation} of a texture in the {@link net.minecraft.client.renderer.texture.TextureManager} used when spyglass scoping.
    public static final LookAssetType<String, ResourceLocation> SCOPE_TEXTURE = register(ScopeTextureLookAsset.INSTANCE);

    /**
     * Returns an immutable view of all available {@link LookAssetType}s.
     *
     * @return An immutable view of all available {@link LookAssetType}s
     */
    public static Set<LookAssetType<?, ?>> values() {
        return ImmutableSet.copyOf(REGISTRY.values());
    }

    /**
     * Finds the {@link LookAssetType} for the provided key.
     *
     * @param key The key of the {@link LookAssetType}
     * @return The {@link LookAssetType} for the provided key, or null if not found
     */
    public static @Nullable LookAssetType<?, ?> get(ResourceLocation key) {
        return REGISTRY.get(key);
    }

    /**
     * Registers a {@link LookAssetType} to the registry.
     *
     * @param type The {@link LookAssetType} to register
     * @return The registered {@link LookAssetType}
     * @param <S> The serialization type of the asset
     * @param <L> The loaded type of the asset
     */
    public static <S, L> LookAssetType<S, L> register(LookAssetType<S, L> type) {
        REGISTRY.put(type.key(), type);
        return type;
    }
}
