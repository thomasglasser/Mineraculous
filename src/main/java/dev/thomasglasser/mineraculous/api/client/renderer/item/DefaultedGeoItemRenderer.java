package dev.thomasglasser.mineraculous.api.client.renderer.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DefaultedGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    private final ResourceLocation texture;

    public DefaultedGeoItemRenderer(ResourceLocation id) {
        super(new DefaultedItemGeoModel<>(id));
        texture = makeTextureLocation(id);
    }

    public static ResourceLocation makeTextureLocation(ResourceLocation id) {
        return id.withPrefix("textures/item/geo/").withSuffix(".png");
    }

    public ResourceLocation getTextureLocation() {
        return texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        return getTextureLocation();
    }
}
