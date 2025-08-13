package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.model.DerbyHatModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BetaTesterLayer<S extends AbstractClientPlayer> extends RenderLayer<S, PlayerModel<S>> {
    private final DerbyHatModel derbyHatModel;

    public BetaTesterLayer(RenderLayerParent<S, PlayerModel<S>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.derbyHatModel = new DerbyHatModel(modelSet.bakeLayer(DerbyHatModel.LAYER_LOCATION));
    }

    @Override
    protected ResourceLocation getTextureLocation(S player) {
        return switch (MineraculousClientUtils.betaChoice(player)) {
            case DERBY_HAT -> DerbyHatModel.TEXTURE;
        };
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S player, float v, float v1, float v2, float v3, float v4, float v5) {
        if (MineraculousClientUtils.renderBetaTesterLayer(player)) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(player)));
            BetaTesterCosmeticOptions cosmetic = MineraculousClientUtils.betaChoice(player);
            ModelPart parent = switch (cosmetic.slot()) {
                case HEAD -> getParentModel().head;
                default -> getParentModel().body;
            };
            parent.translateAndRotate(poseStack);
            switch (cosmetic) {
                case DERBY_HAT -> derbyHatModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
