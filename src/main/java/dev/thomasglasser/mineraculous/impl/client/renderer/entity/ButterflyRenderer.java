package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.entity.animal.Butterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ButterflyRenderer<T extends Butterfly> extends GeoEntityRenderer<T> {
    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(MineraculousConstants.modLoc("butterfly")));
        withScale(0.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        return animatable.getVariant().value().texture();
    }
}
