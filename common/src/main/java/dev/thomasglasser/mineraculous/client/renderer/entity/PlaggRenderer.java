package dev.thomasglasser.mineraculous.client.renderer.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.kwami.Plagg;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class PlaggRenderer<T extends Plagg> extends DynamicGeoEntityRenderer<T>
{
	public PlaggRenderer(EntityRendererProvider.Context renderManager)
	{
		super(renderManager, new DefaultedEntityGeoModel<>(Mineraculous.modLoc("kwami/plagg")));
		withScale(0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(T animatable)
	{
		String path = super.getTextureLocation(animatable).getPath().replace(".png", "");
		if (!animatable.isCharged()) path += "_hungry";
		return Mineraculous.modLoc(path + ".png");
	}
}
