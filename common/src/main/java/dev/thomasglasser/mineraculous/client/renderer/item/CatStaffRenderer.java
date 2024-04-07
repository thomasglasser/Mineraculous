package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.client.renderer.item.PerspectiveAwareGeoItemRenderer;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CatStaffRenderer extends PerspectiveAwareGeoItemRenderer<CatStaffItem>
{
	public CatStaffRenderer()
	{
		super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("cat_staff")), Mineraculous.modLoc("cat_staff"));
	}
}
