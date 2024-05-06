package dev.thomasglasser.mineraculous.client.renderer.entity;

import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class KwamiRenderer<T extends Kwami> extends DynamicGeoEntityRenderer<T>
{
	private ResourceLocation tiredTexture;

	public KwamiRenderer(EntityRendererProvider.Context renderManager, ResourceLocation location)
	{
		super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(location.getNamespace(), "kwami/" + location.getPath())));
		withScale(0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(T animatable)
	{
		if (tiredTexture == null)
		{
			ResourceLocation original = super.getTextureLocation(animatable);
			tiredTexture = new ResourceLocation(original.getNamespace(), original.getPath().replace(".png", "_tired.png"));
		}
		if (!animatable.isCharged())
			return tiredTexture;
		return super.getTextureLocation(animatable);
	}
}
