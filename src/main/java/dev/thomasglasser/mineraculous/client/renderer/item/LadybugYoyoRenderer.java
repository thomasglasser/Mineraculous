package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LadybugYoyoRenderer extends GeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo.png");

    public LadybugYoyoRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")));
    }

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        return TEXTURE;
    }
}
