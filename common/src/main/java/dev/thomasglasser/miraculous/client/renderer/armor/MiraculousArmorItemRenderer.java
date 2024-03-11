package dev.thomasglasser.miraculous.client.renderer.armor;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.world.item.armor.MiraculousArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {

    private final String miraculous;

    public MiraculousArmorItemRenderer(String miraculous) {
        super(new DefaultedItemGeoModel<>(Miraculous.modLoc("armor/" + miraculous + "_miraculous_suit")));
        this.miraculous = miraculous;
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousArmorItem animatable) {
        return Miraculous.modLoc("textures/models/armor/" + miraculous + "_miraculous_suit_default.png");
    }
}
