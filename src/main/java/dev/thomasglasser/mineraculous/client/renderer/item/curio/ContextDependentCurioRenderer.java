package dev.thomasglasser.mineraculous.client.renderer.item.curio;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class ContextDependentCurioRenderer implements ICurioRenderer {
    ItemInHandRenderer renderer;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            poseStack.pushPose();
            ItemDisplayContext displayContext = switch (slotContext.identifier()) {
                case "ring" -> {
                    humanoidModel.translateToHand(ClientUtils.getMinecraft().options.mainHand().get(), poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_RING.getValue();
                }
                case "brooch" -> {
                    humanoidModel.body.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_BROOCH.getValue();
                }
                case "earrings" -> {
                    humanoidModel.head.translateAndRotate(poseStack);
                    yield MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue();
                }
                case "belt" -> {
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
            poseStack.popPose();
        }
    }
}
