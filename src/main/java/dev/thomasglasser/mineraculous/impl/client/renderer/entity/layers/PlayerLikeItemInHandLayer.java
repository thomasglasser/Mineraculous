package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;

public class PlayerLikeItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    private static final float X_ROT_MIN = (-(float) Math.PI / 6F);
    private static final float X_ROT_MAX = ((float) Math.PI / 2F);

    public PlayerLikeItemInHandLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (itemStack.canPerformAction(ItemAbilities.SPYGLASS_SCOPE) && livingEntity.getUseItem() == itemStack && livingEntity.swingTime == 0) {
            this.renderArmWithSpyglass(livingEntity, itemStack, arm, poseStack, buffer, packedLight);
        } else {
            super.renderArmWithItem(livingEntity, itemStack, displayContext, arm, poseStack, buffer, packedLight);
        }
    }

    private void renderArmWithSpyglass(LivingEntity entity, ItemStack stack, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        poseStack.pushPose();
        ModelPart modelpart = this.getParentModel().getHead();
        float f = modelpart.xRot;
        modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
        modelpart.translateAndRotate(poseStack);
        modelpart.xRot = f;
        CustomHeadLayer.translateToHead(poseStack, false);
        boolean flag = arm == HumanoidArm.LEFT;
        poseStack.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        this.itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, poseStack, buffer, combinedLight);
        poseStack.popPose();
    }
}
