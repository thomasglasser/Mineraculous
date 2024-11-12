package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.KamikotizationArmorItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.texture.GeoAbstractTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class KamikotizationArmorItemRenderer extends GeoArmorRenderer<KamikotizationArmorItem> {
    private final Map<ResourceKey<Kamikotization>, GeoModel<KamikotizationArmorItem>> models = new HashMap<>();

    public KamikotizationArmorItemRenderer() {
        super(null);
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            protected @Nullable RenderType getRenderType(KamikotizationArmorItem animatable, MultiBufferSource bufferSource) {
                if (getCurrentStack() != null) {
                    ResourceLocation glowMask = GeoAbstractTexture.appendToPath(getTextureResource(animatable), "_glowmask");
                    if (Minecraft.getInstance().getResourceManager().getResource(glowMask).isPresent()) {
                        return super.getRenderType(animatable, bufferSource);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public GeoModel<KamikotizationArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            ResourceKey<Kamikotization> kamikotization = getCurrentStack().get(MineraculousDataComponents.KAMIKOTIZATION);
            if (kamikotization != null) {
                if (!models.containsKey(kamikotization))
                    models.put(kamikotization, createGeoModel(kamikotization));
                return models.get(kamikotization);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<KamikotizationArmorItem> createGeoModel(ResourceKey<Kamikotization> kamikotization) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(kamikotization.location().getNamespace(), "armor/kamikotization/" + kamikotization.location().getPath())) {
            private final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(kamikotization.location().getNamespace(), "textures/entity/equipment/humanoid/kamikotization/" + kamikotization.location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(KamikotizationArmorItem animatable, GeoRenderer<KamikotizationArmorItem> renderer) {
                return textureLoc;
            }
        };
    }
}
