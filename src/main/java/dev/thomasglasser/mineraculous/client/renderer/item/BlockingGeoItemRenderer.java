package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BlockingGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private final GeoModel<T> blocking;

    public BlockingGeoItemRenderer(GeoModel<T> base, GeoModel<T> blocking) {
        super(base);
        this.blocking = blocking;
    }

    public boolean isBlocking() {
        return getCurrentItemStack() != null && getCurrentItemStack().has(MineraculousDataComponents.BLOCKING);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        if (isBlocking())
            return blocking;
        return super.getGeoModel();
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        if (isBlocking())
            return blocking.getTextureResource(animatable);
        return super.getTextureLocation(animatable);
    }
}
