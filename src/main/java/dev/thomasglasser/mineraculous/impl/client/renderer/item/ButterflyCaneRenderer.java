package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.tommylib.api.client.renderer.item.DefaultedGeoItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class ButterflyCaneRenderer extends DefaultedGeoItemRenderer<ButterflyCaneItem> {
    public static final ResourceLocation SPYGLASS_SCOPE_LOCATION = Mineraculous.modLoc("textures/misc/butterfly_cane_spyglass_scope.png");
    public static final ResourceLocation SPYGLASS_LOCATION = makeTextureLocation(Mineraculous.modLoc("butterfly_cane_spyglass"));
    public static final ResourceLocation PHONE_LOCATION = makeTextureLocation(Mineraculous.modLoc("butterfly_cane_phone"));

    private final GeoModel<ButterflyCaneItem> phoneModel;

    public ButterflyCaneRenderer() {
        super(MineraculousItems.BUTTERFLY_CANE.getId());
        phoneModel = new DefaultedItemGeoModel<>(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone")) {
            @Override
            public ResourceLocation getAnimationResource(ButterflyCaneItem animatable) {
                return ButterflyCaneRenderer.this.model.getAnimationResource(animatable);
            }
        };
    }

    @Override
    public GeoModel<ButterflyCaneItem> getGeoModel() {
        ItemStack stack = getCurrentItemStack();
        ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
        if (ability == ButterflyCaneItem.Ability.PHONE) {
            return phoneModel;
        }
        return super.getGeoModel();
    }

    @Override
    public ResourceLocation getTextureLocation(ButterflyCaneItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
            if (ability == ButterflyCaneItem.Ability.PHONE) {
                return PHONE_LOCATION;
            } else if (ability == ButterflyCaneItem.Ability.SPYGLASS) {
                return SPYGLASS_LOCATION;
            }
        }
        return super.getTextureLocation(animatable);
    }
}
