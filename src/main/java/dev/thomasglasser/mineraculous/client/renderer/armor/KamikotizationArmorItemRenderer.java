package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.DynamicAutoGlowingTexture;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class KamikotizationArmorItemRenderer extends GeoArmorRenderer<KamikotizationArmorItem> {
    private final Map<ResourceKey<Kamikotization>, GeoModel<KamikotizationArmorItem>> defaultModels = new HashMap<>();
    private final Map<ResourceKey<Kamikotization>, GeoModel<KamikotizationArmorItem>> customModels = new HashMap<>();

    public KamikotizationArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(KamikotizationArmorItem animatable, @Nullable MultiBufferSource bufferSource) {
                ResourceLocation texture = getTextureLocation(animatable);
                ResourceLocation glowmaskTexture = AutoGlowingTexture.appendToPath(texture, "_glowmask");
                if (Minecraft.getInstance().getTextureManager().getTexture(glowmaskTexture, MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture()) {
                    if (Minecraft.getInstance().getResourceManager().getResource(glowmaskTexture).isPresent())
                        return super.getRenderType(animatable, bufferSource);
                    else if (getCurrentStack() != null && getCurrentEntity() instanceof LivingEntity livingEntity) {
                        ResourceKey<Kamikotization> kamikotization = getCurrentStack().get(MineraculousDataComponents.KAMIKOTIZATION);
                        KamikotizationLookData data = livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).get(kamikotization);
                        if (data != null) {
                            if (data.glowmask().isPresent() && texture.equals(data.texture())) {
                                byte[] glowmask = data.glowmask().get();
                                try {
                                    DynamicAutoGlowingTexture.register(texture, glowmask);
                                } catch (IOException e) {
                                    Mineraculous.LOGGER.error("Failed to register glowmask texture for {}", texture, e);
                                }
                            }
                        }
                    }
                    return null;
                }
                return super.getRenderType(animatable, bufferSource);
            }
        });
    }

    @Override
    public GeoModel<KamikotizationArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            ResourceKey<Kamikotization> kamikotization = getCurrentStack().get(MineraculousDataComponents.KAMIKOTIZATION);
            if (kamikotization != null) {
                if (!defaultModels.containsKey(kamikotization))
                    defaultModels.put(kamikotization, createDefaultGeoModel(kamikotization));
                if (getCurrentEntity() instanceof Player player) {
                    KamikotizationLookData data = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).get(kamikotization);
                    if (data != null) {
                        if (!customModels.containsKey(kamikotization))
                            customModels.put(kamikotization, createCustomGeoModel(kamikotization, data));
                        return customModels.get(kamikotization);
                    }
                }
                return defaultModels.get(kamikotization);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<KamikotizationArmorItem> createDefaultGeoModel(ResourceKey<Kamikotization> kamikotization) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(kamikotization.location().getNamespace(), "armor/kamikotization/" + kamikotization.location().getPath())) {
            private final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(kamikotization.location().getNamespace(), "textures/entity/equipment/humanoid/kamikotization/" + kamikotization.location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable) {
                return textureLoc;
            }
        };
    }

    private GeoModel<KamikotizationArmorItem> createCustomGeoModel(ResourceKey<Kamikotization> kamikotization, KamikotizationLookData data) {
        return new GeoModel<>() {
            private BakedGeoModel currentModel = null;

            @Override
            public ResourceLocation getModelResource(KamikotizationArmorItem animatable) {
                return data.model().isPresent() ? null : defaultModels.get(kamikotization).getModelResource(animatable);
            }

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable) {
                return data.texture();
            }

            @Override
            public ResourceLocation getAnimationResource(KamikotizationArmorItem animatable) {
                return null;
            }

            @Override
            public BakedGeoModel getBakedModel(ResourceLocation location) {
                BakedGeoModel baked = data.model().orElseGet(() -> defaultModels.get(kamikotization).getBakedModel(location));
                if (currentModel != baked) {
                    currentModel = baked;
                    getAnimationProcessor().setActiveModel(baked);
                }
                return currentModel;
            }
        };
    }
}
