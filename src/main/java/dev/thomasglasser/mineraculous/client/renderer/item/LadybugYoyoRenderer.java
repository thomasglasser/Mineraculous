package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class LadybugYoyoRenderer extends BlockingGeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo.png");

    private static final ResourceLocation SHIELD_TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo_shield.png");

    public LadybugYoyoRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")), new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo_shield")));
    }

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        if (isBlocking())
            return SHIELD_TEXTURE;
        return TEXTURE;
    }
}
