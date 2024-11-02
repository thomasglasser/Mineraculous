package dev.thomasglasser.mineraculous.client.renderer.entity;

import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class KwamiRenderer<T extends Kwami> extends DynamicGeoEntityRenderer<T> {
    private final Map<ResourceKey<Miraculous>, GeoModel<T>> models = new HashMap<>();

    public KwamiRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, null);
        withScale(0.4F);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        if (getAnimatable() != null) {
            ResourceKey<Miraculous> miraculous = getAnimatable().getMiraculous();
            if (miraculous != null) {
                if (!models.containsKey(miraculous))
                    models.put(miraculous, createGeoModel(miraculous));
                return models.get(miraculous);
            }
        }
        return super.getGeoModel();
    }

    private GeoModel<T> createGeoModel(ResourceKey<Miraculous> miraculous) {
        return new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(miraculous.location().getNamespace(), "miraculous/" + miraculous.location().getPath())) {
            private ResourceLocation hungryTexture;

            @Override
            public ResourceLocation getTextureResource(T animatable, GeoRenderer<T> renderer) {
                if (hungryTexture == null) {
                    ResourceLocation original = super.getTextureResource(animatable, renderer);
                    hungryTexture = ResourceLocation.fromNamespaceAndPath(original.getNamespace(), original.getPath().replace(".png", "_hungry.png"));
                }
                if (!animatable.isCharged())
                    return hungryTexture;
                return super.getTextureResource(animatable, renderer);
            }
        };
    }
}
