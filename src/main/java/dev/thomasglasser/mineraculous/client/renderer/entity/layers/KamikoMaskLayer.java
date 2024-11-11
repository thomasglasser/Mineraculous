package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.client.model.KamikoMaskModel;
import dev.thomasglasser.mineraculous.client.renderer.entity.state.MineraculousLivingEntityRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class KamikoMaskLayer<S extends PlayerRenderState> extends RenderLayer<S, PlayerModel> {
    private final KamikoMaskModel model;

    public KamikoMaskLayer(RenderLayerParent<S, PlayerModel> renderer, EntityModelSet modelSet) {
        super(renderer);

        this.model = new KamikoMaskModel(modelSet.bakeLayer(KamikoMaskModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, S renderState, float p_117353_, float p_117354_) {
        if (((MineraculousLivingEntityRenderState) renderState).mineraculous$showKamikoMask()) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(KamikoMaskModel.TEXTURE));
            poseStack.scale(0.625F, 0.625F, 0.625F);
            getParentModel().getHead().translateAndRotate(poseStack);
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
