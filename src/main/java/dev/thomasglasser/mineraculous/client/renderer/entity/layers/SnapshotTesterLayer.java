package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

// TODO: Implement
public class SnapshotTesterLayer<S extends AbstractClientPlayer> extends RenderLayer<S, PlayerModel<S>> {
//    private final PilotsSnapshotTesterHatModel pilotsSnapshotTesterHatModel;
    public SnapshotTesterLayer(RenderLayerParent<S, PlayerModel<S>> renderer, EntityModelSet modelSet) {
        super(renderer);
//        this.pilotsSnapshotTesterHatModel = new PilotsSnapshotTesterHatModel(modelSet.bakeLayer(PilotsSnapshotTesterHatModel.LAYER_LOCATION));
    }

    @Override
    protected ResourceLocation getTextureLocation(S player) {
        return switch (MineraculousClientUtils.snapshotChoice(player)) {
            case null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + MineraculousClientUtils.snapshotChoice(player));
        };
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S player, float v, float v1, float v2, float v3, float v4, float v5) {
//        if (MineraculousClientUtils.renderSnapshotTesterLayer(player)) {
//            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(player)));
//            getParentModel().getHead().translateAndRotate(poseStack);
//            switch (MineraculousClientUtils.snapshotChoice(player)) {
//                case null -> {}
//                default ->
//                        throw new IllegalStateException("Unexpected value: " + MineraculousClientUtils.snapshotChoice(player));
//            }
//        }
    }
}
