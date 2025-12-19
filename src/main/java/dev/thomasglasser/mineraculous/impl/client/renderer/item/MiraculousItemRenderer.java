package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MiraculousItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private static final Set<MiraculousItemRenderer<?>> INSTANCES = new ReferenceOpenHashSet<>();
    private static final Map<ResourceKey<Miraculous>, ModelResourceLocation> MODEL_LOCATIONS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, EnumMap<MiraculousItem.TextureState, ResourceLocation>> POWERED_FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> POWERED_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> HIDDEN_TEXTURES = new Object2ReferenceOpenHashMap<>();

    private final Map<Holder<Miraculous>, GeoModel<?>> models = new Object2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        INSTANCES.add(this);
    }

    public static void clearAssets() {
        MODEL_LOCATIONS.clear();
        POWERED_FRAME_TEXTURES.clear();
        POWERED_TEXTURES.clear();
        HIDDEN_TEXTURES.clear();

        INSTANCES.forEach(renderer -> renderer.models.clear());
        INSTANCES.clear();
    }

    public static Holder<Miraculous> getMiraculousOrDefault(ItemStack stack) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        Level level = ClientUtils.getLevel();
        if (miraculous == null && level != null) {
            miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
        }
        if (miraculous == null) {
            throw new IllegalStateException("Tried to render a Miraculous related item without any registered miraculous");
        }
        return miraculous;
    }

    public static <T> T getLookAsset(ItemStack stack, Holder<Miraculous> miraculous, Holder<LookContext> context, LookAssetType<T> assetType, Supplier<T> fallback) {
        Integer carrier = stack.get(MineraculousDataComponents.CARRIER);
        Level level = ClientUtils.getLevel();
        if (carrier != null && level != null && level.getEntities().get(carrier) instanceof Player player) {
            T asset = LookManager.getOrFetchLookAsset(player, miraculous, context, assetType);
            if (asset != null)
                return asset;
        }
        return fallback.get();
    }

    public static Holder<LookContext> getContext(ItemStack stack) {
        return stack.get(MineraculousDataComponents.TEXTURE_STATE) == MiraculousItem.TextureState.HIDDEN ? LookContexts.HIDDEN_MIRACULOUS : LookContexts.ACTIVE_MIRACULOUS;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            boolean transformed = false;
            ItemStack stack = getCurrentItemStack();
            ItemTransforms transforms = getLookAsset(stack, getMiraculousOrDefault(stack), getContext(stack), LookAssetTypes.ITEM_TRANSFORMS, () -> null);
            if (transforms != null && transforms.hasTransform(renderPerspective)) {
                transformed = true;
                transforms.getTransform(renderPerspective).apply(false, poseStack);
            }
            if (!transformed) {
                BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(MODEL_LOCATIONS.computeIfAbsent(getMiraculousOrDefault(stack).getKey(), key -> ModelResourceLocation.standalone(key.location().withPrefix("item/miraculous/"))));
                if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                    miraculousModel.applyTransform(renderPerspective, poseStack, false);
                }
            }
        }
        // Special case for earrings
        model.getBone("left_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_RIGHT_EARRING.getValue()));
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()));
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        MiraculousItem.TextureState state = getCurrentItemStack().get(MineraculousDataComponents.TEXTURE_STATE);
        ResourceLocation base = super.getTextureLocation(animatable);
        if (state == null || state == MiraculousItem.TextureState.POWERED) {
            return POWERED_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "powered")));
        }
        if (state == MiraculousItem.TextureState.ACTIVE) {
            return base;
        }
        if (state == MiraculousItem.TextureState.HIDDEN) {
            return HIDDEN_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "hidden")));
        }
        return POWERED_FRAME_TEXTURES.computeIfAbsent(base, loc -> new EnumMap<>(MiraculousItem.TextureState.class)).computeIfAbsent(state, i -> base.withPath(path -> path.replace("active", "powered_" + i.frame())));
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return (GeoModel<T>) models.computeIfAbsent(getMiraculousOrDefault(getCurrentItemStack()), this::createGeoModel);
    }

    private GeoModel<T> createGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("miraculous/")) {
            private final ResourceLocation defaultTexture = miraculous.getKey().location().withPath(path -> "textures/item/miraculous/" + path + "/active.png");

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                ItemStack stack = getCurrentItemStack();
                return getLookAsset(stack, miraculous, getContext(stack), LookAssetTypes.GECKOLIB_MODEL, () -> super.getBakedModel(location));
            }

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                ItemStack stack = getCurrentItemStack();
                return getLookAsset(stack, miraculous, getContext(stack), LookAssetTypes.TEXTURE, () -> defaultTexture);
            }

            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                ItemStack stack = getCurrentItemStack();
                BakedAnimations animations = getLookAsset(stack, miraculous, getContext(stack), LookAssetTypes.GECKOLIB_ANIMATIONS, () -> null);
                if (animations != null) {
                    Animation animation = animations.getAnimation(name);
                    if (animation != null)
                        return animation;
                }

                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }
}
