package dev.thomasglasser.mineraculous.client.renderer.item.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CatMiraculousItemCurioRenderer implements ICurioRenderer
{
	ItemInHandRenderer renderer;

	@Override
	public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		poseStack.pushPose();
		if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)
		{
			humanoidModel.translateToHand(HumanoidArm.RIGHT, poseStack);
		}
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate(1 / 16.0F, -0.05F, -0.625F);
		if (renderer == null)
			renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
		renderer.renderItem(slotContext.entity(), stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, renderTypeBuffer, light);
		poseStack.popPose();
	}
}
