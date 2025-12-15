package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.mineraculous.impl.client.look.MiraculousLook;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
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
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        ItemStack stack = getCurrentStack();
        Holder<Miraculous> miraculous = MiraculousItemRenderer.getMiraculousOrDefault(stack);
        Optional<Integer> transformationFrames = miraculous.value().transformationFrames();
        if (transformationFrames.isPresent()) {
            MiraculousData.TransformationState transformationState = stack.get(MineraculousDataComponents.TRANSFORMATION_STATE);
            if (transformationState != null) {
                int remainingFrames = transformationState.remainingFrames();
                int frame = transformationState.transforming() ? transformationFrames.get() - remainingFrames : remainingFrames;
                if (frame >= 0) {
                    ResourceLocation texture = FRAME_TEXTURES.computeIfAbsent(base, loc -> new Int2ObjectOpenHashMap<>()).computeIfAbsent(frame, i -> base.withPath(path -> path.replace(".png", "_" + i + ".png")));
                    if (MineraculousClientUtils.isValidTexture(texture))
                        return texture;
                }
            }
        }
        return base;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return (GeoModel<T>) models.computeIfAbsent(MiraculousItemRenderer.getMiraculousOrDefault(getCurrentStack()), this::createGeoModel);
    }

    private GeoModel<T> createGeoModel(Holder<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(miraculous.getKey().location().withPrefix("armor/miraculous/")) {
            private final ResourceLocation texture = miraculous.getKey().location().withPath(path -> "textures/entity/equipment/humanoid/miraculous/" + path + ".png");

            private BakedGeoModel currentModel = null;

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel model = null;
                if (getCurrentEntity() instanceof Player player) {
                    MiraculousLook look = LookManager.getLook(player.getUUID(), miraculous);
                    if (look != null) {
                        model = look.getModel(MiraculousLook.AssetType.SUIT, () -> super.getBakedModel(location));
                    }
                }
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
                if (getCurrentEntity() instanceof Player player) {
                    MiraculousLook look = LookManager.getLook(player.getUUID(), miraculous);
                    if (look != null)
                        return look.getTexture(MiraculousLook.AssetType.SUIT, () -> texture);
                }
                return texture;
            }

            @Override
            public @Nullable Animation getAnimation(T animatable, String name) {
                if (getCurrentEntity() instanceof Player player) {
                    MiraculousLook look = LookManager.getLook(player.getUUID(), miraculous);
                    if (look != null) {
                        BakedAnimations animations = look.getAnimations(MiraculousLook.AssetType.SUIT, () -> null);
                        if (animations != null) {
                            Animation animation = animations.getAnimation(name);
                            if (animation != null)
                                return animation;
                        }
                    }
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
