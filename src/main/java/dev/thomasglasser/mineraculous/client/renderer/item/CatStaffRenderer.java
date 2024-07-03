package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.client.renderer.item.PerspectiveAwareGeoItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CatStaffRenderer extends PerspectiveAwareGeoItemRenderer<CatStaffItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/cat_staff.png");

    public CatStaffRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("cat_staff")), Mineraculous.modLoc("cat_staff"));
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            static RenderType RENDER_TYPE;

            @Override
            protected RenderType getRenderType(CatStaffItem animatable) {
                if (RENDER_TYPE == null) {
                    RENDER_TYPE = RenderType.eyes(AutoGlowingTexture.getEmissiveResource(TEXTURE));
                }
                return RENDER_TYPE;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(CatStaffItem animatable) {
        return TEXTURE;
    }
}
