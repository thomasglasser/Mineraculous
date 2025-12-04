package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class PlayerLikeDeadmau5EarsLayer<T extends LivingEntity & PlayerLike> extends RenderLayer<T, PlayerModel<T>> {
    public PlayerLikeDeadmau5EarsLayer(RenderLayerParent<T, PlayerModel<T>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if ("deadmau5".equals(livingEntity.getName().getString()) && !livingEntity.isInvisible() && livingEntity.getVisualSource() instanceof AbstractClientPlayer player) {
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(player.getSkin().texture()));
            int i = LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F);

            for (int j = 0; j < 2; ++j) {
                float f = Mth.lerp(partialTicks, livingEntity.yRotO, livingEntity.getYRot()) - Mth.lerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                float f1 = Mth.lerp(partialTicks, livingEntity.xRotO, livingEntity.getXRot());
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(f));
                poseStack.mulPose(Axis.XP.rotationDegrees(f1));
                poseStack.translate(0.375F * (float) (j * 2 - 1), 0.0F, 0.0F);
                poseStack.translate(0.0F, -0.375F, 0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-f1));
                poseStack.mulPose(Axis.YP.rotationDegrees(-f));
                float f2 = 1.3333334F;
                poseStack.scale(f2, f2, f2);
                this.getParentModel().renderEars(poseStack, vertexconsumer, packedLight, i);
                poseStack.popPose();
            }
        }
    }
}
