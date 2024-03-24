package dev.thomasglasser.mineraculous.client.renderer.item.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MiraculousItemCurioRenderer
{
	public void render(ItemStack stack, LivingEntity entity, HumanoidModel<?> model, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
	{
		if (stack.is(MineraculousItems.CAT_MIRACULOUS.get()))
		{
			renderCat(stack, entity, model, poseStack, buffer, packedLight);
		}
	}

	private void renderCat(ItemStack stack, LivingEntity entity, HumanoidModel<?> model, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
	{
		poseStack.pushPose();
		model.translateToHand(HumanoidArm.RIGHT, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate(1 / 16.0F, -0.05F, -0.625F);
		ItemInHandRenderer renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
		renderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
