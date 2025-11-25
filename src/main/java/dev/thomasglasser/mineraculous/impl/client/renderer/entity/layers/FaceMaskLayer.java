package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.model.FaceMaskModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

public class FaceMaskLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    private final FaceMaskModel model;

    public FaceMaskLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer);

        this.model = new FaceMaskModel(modelSet.bakeLayer(FaceMaskModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, T livingEntity, float v, float v1, float v2, float v3, float v4, float v5) {
        livingEntity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).faceMaskTexture().ifPresent(texture -> {
            poseStack.pushPose();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(texture));
            poseStack.scale(0.625F, 0.625F, 0.625F);
            getParentModel().getHead().translateAndRotate(poseStack);
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        });
    }
}
