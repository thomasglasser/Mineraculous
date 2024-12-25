package dev.thomasglasser.mineraculous.client.renderer.item.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CatStaffCurioRenderer implements ICurioRenderer {
    ItemInHandRenderer renderer;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        if (slotContext.entity().isCrouching()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(-0.7, -0.05, -0.21);
        } else {
            poseStack.translate(-0.55, -0.05, 0.09);
        }
        if (renderer == null)
            renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
        renderer.renderItem(slotContext.entity(), stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, renderTypeBuffer, light);
        poseStack.popPose();
    }
}
