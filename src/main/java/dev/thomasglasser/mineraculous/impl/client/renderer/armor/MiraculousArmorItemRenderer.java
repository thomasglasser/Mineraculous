package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {
    private static final Set<MiraculousArmorItemRenderer<?>> INSTANCES = new ReferenceOpenHashSet<>();
    private static final Map<ResourceLocation, Int2ObjectMap<ResourceLocation>> FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();

    private final Map<Holder<Miraculous>, GeoModel<?>> models = new Object2ReferenceOpenHashMap<>();

    private @Nullable BakedGeoModel model = null;
    private @Nullable ResourceLocation texture = null;
    private @Nullable BakedAnimations animations = null;
    private @Nullable Int2ObjectMap<ResourceLocation> frameTextures = null;

    public MiraculousArmorItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        INSTANCES.add(this);
    }

    public static void clearAssets() {
        FRAME_TEXTURES.clear();
        INSTANCES.forEach(renderer -> renderer.models.clear());
        INSTANCES.clear();
    }

    @Override
    public void prepForRender(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel, MultiBufferSource bufferSource, float partialTick, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        super.prepForRender(entity, stack, slot, baseModel, bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        if (entity instanceof Player player) {
            Holder<Miraculous> miraculous = MiraculousItemRenderer.getMiraculousOrDefault(stack);
            model = LookManager.getOrFetchLookAsset(player, miraculous, LookContexts.MIRACULOUS_SUIT, LookAssetTypes.GECKOLIB_MODEL);
            texture = LookManager.getOrFetchLookAsset(player, miraculous, LookContexts.MIRACULOUS_SUIT, LookAssetTypes.TEXTURE);
            animations = LookManager.getOrFetchLookAsset(player, miraculous, LookContexts.MIRACULOUS_SUIT, LookAssetTypes.GECKOLIB_ANIMATIONS);
            frameTextures = LookManager.getOrFetchLookAsset(player, miraculous, LookContexts.MIRACULOUS_SUIT, LookAssetTypes.TEXTURE_FRAMES);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        ItemStack stack = getCurrentStack();
        return MiraculousItemRenderer.getMiraculousOrDefault(stack).value().transformationFrames().map(frames -> {
            MiraculousData.TransformationState transformationState = stack.get(MineraculousDataComponents.TRANSFORMATION_STATE);
            if (transformationState != null) {
                int remainingFrames = transformationState.remainingFrames();
                int frame = transformationState.transforming() ? frames - remainingFrames : remainingFrames;
                if (frame >= 0) {
                    ResourceLocation texture = frameTextures != null ? frameTextures.get(frame) : null;
                    if (texture == null)
                        texture = FRAME_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(frame, i -> base.withPath(path -> path.replace(".png", "_" + i + ".png")));
                    if (MineraculousClientUtils.isValidTexture(texture))
                        return texture;
                }
            }
            return base;
        }).orElse(base);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return (GeoModel<T>) models.computeIfAbsent(MiraculousItemRenderer.getMiraculousOrDefault(getCurrentStack()), this::createGeoModel);
    }

    private GeoModel<T> createGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("armor/miraculous/")) {
            private final ResourceLocation defaultTexture = miraculous.getKey().location().withPath(path -> "textures/entity/equipment/humanoid/miraculous/" + path + ".png");

            private BakedGeoModel currentModel = null;

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel model = MiraculousArmorItemRenderer.this.model;
                if (model == null)
                    model = super.getBakedModel(location);
                if (model != this.currentModel) {
                    this.getAnimationProcessor().setActiveModel(model);
                    this.currentModel = model;
                }
                return currentModel;
            }

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                if (texture == null)
                    return defaultTexture;
                return texture;
            }

            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                if (animations != null) {
                    Animation animation = animations.getAnimation(name);
                    if (animation != null)
                        return animation;
                }

                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException ignored) {
                    return null;
                }
            }
        };
    }
}
