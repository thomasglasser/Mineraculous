package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CatStaffRenderer extends GeoItemRenderer<CatStaffItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/cat_staff.png");

    public CatStaffRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("cat_staff")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CatStaffItem animatable) {
        return TEXTURE;
    }
}
