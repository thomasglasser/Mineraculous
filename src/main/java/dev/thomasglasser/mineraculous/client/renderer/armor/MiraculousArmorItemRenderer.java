package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {
    private final Map<ResourceKey<Miraculous>, GeoModel<MiraculousArmorItem>> models = new HashMap<>();

    public MiraculousArmorItemRenderer() {
        super(null);
        // TODO: Glowmask fix
//        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
//            @Override
//            protected @Nullable RenderType getRenderType(MiraculousArmorItem animatable, MultiBufferSource bufferSource) {
//                if (getCurrentStack() != null) {
//                    ResourceLocation glowMask = GeoAbstractTexture.appendToPath(getTextureResource(animatable), "_glowmask");
//                    if (Minecraft.getInstance().getTextureManager().getTexture(glowMask, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture()) {
//                        return super.getRenderType(animatable, bufferSource);
//                    }
//                }
//                return null;
//            }
//        });
    }

    @Override
    public GeoModel<MiraculousArmorItem> getGeoModel() {
        if (getCurrentStack() != null) {
            ResourceKey<Miraculous> miraculous = getCurrentStack().get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                if (!models.containsKey(miraculous))
                    models.put(miraculous, createGeoModel(miraculous));
                return models.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<MiraculousArmorItem> createGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "armor/miraculous/" + miraculous.location().getPath())) {
            private final ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "textures/models/armor/miraculous/" + miraculous.location().getPath() + ".png");

            @Override
            public ResourceLocation getTextureResource(MiraculousArmorItem animatable, GeoRenderer<MiraculousArmorItem> renderer) {
                return textureLoc;
            }
        };
    }
}
