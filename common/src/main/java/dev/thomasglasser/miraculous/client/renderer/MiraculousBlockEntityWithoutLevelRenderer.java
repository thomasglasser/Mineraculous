package dev.thomasglasser.miraculous.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.client.MiraculousClientConfig;
import dev.thomasglasser.miraculous.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MiraculousBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer
{

	public MiraculousBlockEntityWithoutLevelRenderer()
	{
		super(null, null);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
	{
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		if (stack.getItem() instanceof MiraculousItem miraculousItem)
		{
			ResourceLocation loc = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (!miraculousItem.isPowered(stack) && !miraculousItem.getHolder(stack).isEmpty() && MiraculousClientConfig.enableCustomHiddenVariants)
			{
				TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, Miraculous.MOD_ID, loc.getPath() + "_" + miraculousItem.getHolder(stack).toLowerCase(), loc.getPath() + "_default");
			}
			else if (miraculousItem.isPowered(stack))
			{
				TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, Miraculous.MOD_ID, loc.getPath() + "_powered");
			}
			else
			{
				TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, Miraculous.MOD_ID, loc.getPath() + "_default");
			}
		}
		poseStack.popPose();
	}
}
