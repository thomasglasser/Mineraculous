package dev.thomasglasser.mineraculous.api.client.renderer.item.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
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

/**
 * Renders a curio adapted to its slot {@link ItemDisplayContext}.
 *
 * @see MineraculousItemDisplayContexts
 */
public class ContextDependentCurioRenderer implements ICurioRenderer {
    ItemInHandRenderer renderer;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            poseStack.pushPose();
            ItemDisplayContext displayContext = switch (slotContext.identifier()) {
                case MineraculousCuriosProvider.SLOT_RING -> {
                    humanoidModel.translateToHand(HumanoidArm.RIGHT, poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_RING.getValue();
                }
                case MineraculousCuriosProvider.SLOT_BROOCH -> {
                    humanoidModel.body.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_BROOCH.getValue();
                }
                case MineraculousCuriosProvider.SLOT_EARRINGS -> {
                    humanoidModel.head.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue();
                }
                case MineraculousCuriosProvider.SLOT_BELT -> {
                    humanoidModel.body.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_BELT.getValue();
                }
                default -> {
                    humanoidModel.body.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_OTHER.getValue();
                }
            };
            if (renderer == null)
                renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
            renderer.renderItem(slotContext.entity(), stack, displayContext, false, poseStack, renderTypeBuffer, light);
            if (slotContext.identifier().equals("earrings")) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(0, 0, 1 / 16f);
                renderer.renderItem(slotContext.entity(), stack, displayContext, false, poseStack, renderTypeBuffer, light);
            }
            poseStack.popPose();
        }
    }
}
