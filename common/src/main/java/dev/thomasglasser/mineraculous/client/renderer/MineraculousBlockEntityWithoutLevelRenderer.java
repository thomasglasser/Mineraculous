package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MineraculousBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer
{

	public MineraculousBlockEntityWithoutLevelRenderer()
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
			String basePath = "miraculous/" + loc.getPath();
			if (!miraculousItem.isPowered(stack) && !miraculousItem.getHolder(stack).isEmpty() && MineraculousClientConfig.enableCustomHiddenVariants)
			{
				TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_" + miraculousItem.getHolder(stack).toLowerCase(), basePath + "_hidden");
			}
			else if (miraculousItem.isPowered(stack))
			{
				int ticks = stack.getOrCreateTag().getInt(MiraculousItem.TAG_REMAININGTICKS);
				final int second = ticks / 20;
				final int minute = (second / 60) + 1;
				if (ticks > 0 && ticks < MiraculousItem.FIVE_MINUTES)
				{
					// To make it blink every other second
					if (second % 2 == 0)
						TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered_" + (minute - 1));
					// First blink level should reference the normal powered model
					else if (minute == 5)
						TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered");
					else
						TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered_" + minute);
				}
				else
				{
					TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered");
				}
			}
			else
			{
				TommyLibServices.ITEM.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_hidden");
			}
		}
		poseStack.popPose();
	}
}
