package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.texture.DynamicAutoGlowingTexture;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class KamikotizationArmorItemRenderer extends GeoArmorRenderer<KamikotizationArmorItem> {
    private static final Map<Holder<Kamikotization>, GeoModel<KamikotizationArmorItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<KamikotizationLookData, GeoModel<KamikotizationArmorItem>> LOOK_MODELS = new Reference2ReferenceOpenHashMap<>();

    public KamikotizationArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(KamikotizationArmorItem animatable, @Nullable MultiBufferSource bufferSource) {
                ResourceLocation texture = getTextureLocation(animatable);
                ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(texture, "_glowmask");
                if (Minecraft.getInstance().getResourceManager().getResource(glowmaskTexture).isPresent() || Minecraft.getInstance().getTextureManager().getTexture(glowmaskTexture, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture()) {
                    return super.getRenderType(animatable, bufferSource);
                } else {
                    ItemStack stack = getCurrentStack();
                    if (stack != null && getCurrentEntity() instanceof LivingEntity livingEntity) {
                        Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
                        KamikotizationLookData data = livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).get(kamikotization);
                        if (data != null) {
                            data.glowmask().ifPresent(glowmask -> {
                                if (texture.equals(data.texture())) {
                                    try {
                                        DynamicAutoGlowingTexture.register(texture, glowmask);
                                    } catch (IOException e) {
                                        Mineraculous.LOGGER.error("Failed to register glowmask texture for {}", texture, e);
                                    }
                                }
                            });
                        }
                    }
                    return null;
                }
            }
        });
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
        LOOK_MODELS.clear();
    }

    @Override
    public GeoModel<KamikotizationArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            Holder<Kamikotization> kamikotization = getCurrentStack().get(MineraculousDataComponents.KAMIKOTIZATION);
            if (kamikotization != null) {
                if (!DEFAULT_MODELS.containsKey(kamikotization))
                    DEFAULT_MODELS.put(kamikotization, createDefaultGeoModel(kamikotization));
                if (getCurrentEntity() instanceof Player player) {
                    KamikotizationLookData data = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).get(kamikotization);
                    if (data != null) {
                        if (!LOOK_MODELS.containsKey(data))
                            LOOK_MODELS.put(data, createLookGeoModel(kamikotization, data));
                        return LOOK_MODELS.get(data);
                    }
                }
                return DEFAULT_MODELS.get(kamikotization);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<KamikotizationArmorItem> createDefaultGeoModel(Holder<Kamikotization> kamikotization) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(kamikotization.getKey().location().getNamespace(), "armor/kamikotization/" + kamikotization.getKey().location().getPath())) {
            private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(kamikotization.getKey().location().getNamespace(), "textures/entity/equipment/humanoid/kamikotization/" + kamikotization.getKey().location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable) {
                return texture;
            }

            @Override
            public @Nullable Animation getAnimation(KamikotizationArmorItem animatable, String name) {
                try {
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }

    private GeoModel<KamikotizationArmorItem> createLookGeoModel(Holder<Kamikotization> kamikotization, KamikotizationLookData data) {
        return new GeoModel<>() {
            private BakedGeoModel currentModel = null;

            @Override
            public ResourceLocation getModelResource(KamikotizationArmorItem animatable) {
                return DEFAULT_MODELS.get(kamikotization).getModelResource(animatable);
            }

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable) {
                return data.texture();
            }

            @Override
            public ResourceLocation getAnimationResource(KamikotizationArmorItem animatable) {
                return DEFAULT_MODELS.get(kamikotization).getAnimationResource(animatable);
            }

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel baked = data.model().orElseGet(() -> super.getBakedModel(location));
                if (currentModel != baked) {
                    currentModel = baked;
                    getAnimationProcessor().setActiveModel(baked);
                }
                return currentModel;
            }

            @Override
            public @Nullable Animation getAnimation(KamikotizationArmorItem animatable, String name) {
                try {
                    if (data.animations().isPresent()) {
                        return data.animations().get().getAnimation(name);
                    }
                    return super.getAnimation(animatable, name);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        };
    }
}
