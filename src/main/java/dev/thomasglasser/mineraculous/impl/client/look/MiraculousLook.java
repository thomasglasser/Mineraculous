package dev.thomasglasser.mineraculous.impl.client.look;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.EnumMap;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;

public record MiraculousLook(String hash, boolean isBuiltIn, String name, String author, Set<ResourceKey<Miraculous>> validMiraculouses, EnumMap<AssetType, BakedGeoModel> models, EnumMap<AssetType, ResourceLocation> textures, EnumMap<AssetType, BakedAnimations> animations, EnumMap<AssetType, ItemTransforms> transforms) {

    public BakedGeoModel getModel(AssetType type, Supplier<BakedGeoModel> fallback) {
        BakedGeoModel model = models.get(type);
        if (model == null)
            return fallback.get();
        return model;
    }

    public ResourceLocation getTexture(AssetType type, Supplier<ResourceLocation> fallback) {
        ResourceLocation texture = textures.get(type);
        if (texture == null)
            return fallback.get();
        return texture;
    }

    public BakedAnimations getAnimations(AssetType type, Supplier<BakedAnimations> fallback) {
        BakedAnimations animations = this.animations.get(type);
        if (animations == null)
            return fallback.get();
        return animations;
    }

    public ItemTransforms getTransforms(AssetType type, Supplier<ItemTransforms> fallback) {
        if (!type.hasTransforms()) {
            MineraculousConstants.LOGGER.warn("Tried to fetch transforms for invalid asset type {}", type.getSerializedName());
            return fallback.get();
        }
        ItemTransforms transforms = this.transforms.get(type);
        if (transforms == null)
            return fallback.get();
        return transforms;
    }
    public enum AssetType implements StringRepresentable {
        SUIT(false),
        TOOL(true),
        JEWEL_ACTIVE(true),
        JEWEL_HIDDEN(true);

        public static final Codec<AssetType> CODEC = StringRepresentable.fromEnum(AssetType::values);

        private final boolean hasTransforms;

        AssetType(boolean hasTransforms) {
            this.hasTransforms = hasTransforms;
        }

        public boolean hasTransforms() {
            return hasTransforms;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static @Nullable AssetType of(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
