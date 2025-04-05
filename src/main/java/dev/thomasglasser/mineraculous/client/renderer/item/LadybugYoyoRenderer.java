package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LadybugYoyoRenderer extends GeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo.png");

    private static final DefaultedItemGeoModel<LadybugYoyoItem> SHIELD_MODEL = new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo_shield"));
    private static final ResourceLocation SHIELD_TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo_shield.png");

    public LadybugYoyoRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")));
    }

    private boolean isBlocking() {
        return getCurrentItemStack() != null && getCurrentItemStack().has(MineraculousDataComponents.BLOCKING);
    }

    @Override
    public GeoModel<LadybugYoyoItem> getGeoModel() {
        if (isBlocking())
            return SHIELD_MODEL;
        return super.getGeoModel();
    }

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        if (isBlocking())
            return SHIELD_TEXTURE;
        return TEXTURE;
    }
}
