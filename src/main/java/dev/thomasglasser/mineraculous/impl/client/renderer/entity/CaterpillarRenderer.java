package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.entity.animal.ButterflyVariant;
import dev.thomasglasser.mineraculous.impl.world.entity.animal.Caterpillar;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CaterpillarRenderer<T extends Caterpillar> extends GeoEntityRenderer<T> {
    public CaterpillarRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(MineraculousConstants.modLoc("caterpillar")));
        withScale(0.1F);
    }

    @Override
    public float getMotionAnimThreshold(T animatable) {
        return 0.005F;
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        Holder<ButterflyVariant> variant = animatable.getVariant();
        return variant.value().caterpillarTexture().orElseThrow(() -> new IllegalStateException("Tried to render caterpillar for invalid variant " + variant.getKey()));
    }
}
