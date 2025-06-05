package dev.thomasglasser.mineraculous.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

// TODO: Convert to Curio Renderer by allowing kwami in item form on shoulder
public class KwamiOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
    private static final Map<CompoundTag, Kwami> KWAMIS = new Object2ObjectOpenHashMap<>();

    public KwamiOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> renderer) {
        super(renderer);
    }

    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        this.render(poseStack, buffer, packedLight, livingEntity, netHeadYaw, true);
        this.render(poseStack, buffer, packedLight, livingEntity, netHeadYaw, false);
    }

    private void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T livingEntity,
            float netHeadYaw,
            boolean leftShoulder) {
//        CompoundTag compound = leftShoulder ? livingEntity.getShoulderEntityLeft() : livingEntity.getShoulderEntityRight();
//        if (!compound.isEmpty() && EntityType.by(compound).isPresent() && EntityType.by(compound).get() == MineraculousEntityTypes.KWAMI.get()) {
//            Kwami kwami = KWAMIS.computeIfAbsent(compound, c -> {
//                Kwami k = new Kwami(MineraculousEntityTypes.KWAMI.get(), Minecraft.getInstance().level);
//                k.setNoAi(true);
//                k.load(c);
//                k.setOnShoulder();
//                return k;
//            });
//            kwami.yBodyRot = netHeadYaw;
//            kwami.yBodyRotO = netHeadYaw;
//            poseStack.pushPose();
//            poseStack.translate(leftShoulder ? 0.33F : -0.33F, livingEntity.isCrouching() ? 0.3 : 0.1, 0.0F);
//            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
//
//            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(kwami).render(kwami, 0, 0, poseStack, buffer, packedLight);
//            poseStack.popPose();
//        }
    }
}
