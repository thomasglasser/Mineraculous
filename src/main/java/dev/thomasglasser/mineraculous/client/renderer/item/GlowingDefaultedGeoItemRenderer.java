package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class GlowingDefaultedGeoItemRenderer extends DefaultedGeoItemRenderer<CatStaffItem> {
    public GlowingDefaultedGeoItemRenderer(ResourceLocation id) {
        super(id);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
