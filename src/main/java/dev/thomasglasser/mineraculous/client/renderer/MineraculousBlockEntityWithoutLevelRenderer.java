package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
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
			if (!stack.has(MineraculousDataComponents.POWERED.get()) && stack.has(DataComponents.PROFILE) && MineraculousClientConfig.enablePerPlayerCustomization)
			{
				ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_" + stack.get(DataComponents.PROFILE).name().orElse("hidden").toLowerCase(), basePath + "_hidden");
			}
			else if (stack.has(MineraculousDataComponents.POWERED.get()))
			{
				int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0);
				final int second = ticks / 20;
				final int minute = (second / 60) + 1;
				if (ticks > 0 && ticks < MiraculousItem.FIVE_MINUTES)
				{
					// To make it blink every other second
					if (second % 2 == 0)
						ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered_" + (minute - 1));
					// First blink level should reference the normal powered model
					else if (minute == 5)
						ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered");
					else
						ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered_" + minute);
				}
				else
				{
					ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_powered");
				}
			}
			else
			{
				ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, loc.getNamespace(), basePath + "_hidden");
			}
		}
		poseStack.popPose();
	}
}
