package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ButterflyCaneRenderer extends GeoItemRenderer<ButterflyCaneItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/butterfly_cane.png");

    public ButterflyCaneRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("butterfly_cane")));
    }

    @Override
    public ResourceLocation getTextureLocation(ButterflyCaneItem animatable) {
        return TEXTURE;
    }
}
