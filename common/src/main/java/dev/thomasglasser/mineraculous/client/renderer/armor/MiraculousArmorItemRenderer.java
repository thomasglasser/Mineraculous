package dev.thomasglasser.mineraculous.client.renderer.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer extends GeoArmorRenderer<MiraculousArmorItem> {

    private final String miraculous;

    public MiraculousArmorItemRenderer(String miraculous) {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("armor/" + miraculous + "_miraculous_suit")));
        this.miraculous = miraculous;
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousArmorItem animatable) {
        return Mineraculous.modLoc("textures/models/armor/" + miraculous + "_miraculous_suit_default.png");
    }
}
