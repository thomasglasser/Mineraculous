package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class KamikotizationArmorItemRenderer extends GeoArmorRenderer<KamikotizationArmorItem> {
    private static final Map<Holder<Kamikotization>, GeoModel<KamikotizationArmorItem>> DEFAULT_MODELS = new Reference2ReferenceOpenHashMap<>();

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
                    return null;
                }
            }
        });
    }

    public static void clearModels() {
        DEFAULT_MODELS.clear();
    }

    @Override
    public GeoModel<KamikotizationArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            Holder<Kamikotization> kamikotization = getCurrentStack().get(MineraculousDataComponents.KAMIKOTIZATION);
            if (kamikotization != null) {
                if (!DEFAULT_MODELS.containsKey(kamikotization))
                    DEFAULT_MODELS.put(kamikotization, createDefaultGeoModel(kamikotization));
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
}
