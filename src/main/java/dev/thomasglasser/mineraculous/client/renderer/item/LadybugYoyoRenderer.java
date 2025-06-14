package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

public class LadybugYoyoRenderer extends BlockingDefaultedGeoItemRenderer<LadybugYoyoItem> {
    // Hand Rendering Constants
    private static final Quaternionf HAND_XP_ROT = Axis.XP.rotationDegrees(-40);
    private static final Quaternionf RIGHT_HAND_YP_ROT = Axis.YP.rotationDegrees(110);
    private static final Quaternionf LEFT_HAND_YP_ROT = Axis.YP.rotationDegrees(-110);
    private static final Quaternionf RIGHT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(-10);
    private static final Quaternionf LEFT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(10);
    private static final Quaternionf POST_HAND_RENDER_ROT = new Quaternionf(new AxisAngle4d(-1, -1, 0.3f, 0.1f));

    public LadybugYoyoRenderer() {
        super(MineraculousItems.LADYBUG_YOYO.getId());
    }

    @Override
    public void defaultRender(PoseStack poseStack, LadybugYoyoItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
            ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
            if (thrownYoyo != null) {
                InteractionHand initialHand = thrownYoyo.getInitialHand();
                InteractionHand currentHand = switch (player.getMainArm()) {
                    case LEFT -> this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    case RIGHT -> this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                };
                if (initialHand == currentHand) {
                    renderHand(player, this.renderPerspective, poseStack, bufferSource, packedLight);
                }
                poseStack.scale(0.001f, 0.001f, 0.001f);
            }
        }
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    private static void renderHand(AbstractClientPlayer player, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        poseStack.scale(2, 2, 2);
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.translate(0.5, -0.5, -0.2);
            poseStack.mulPose(HAND_XP_ROT);
            poseStack.mulPose(RIGHT_HAND_YP_ROT);
            poseStack.mulPose(RIGHT_HAND_ZP_ROT);
            playerRenderer.renderRightHand(poseStack, multiBufferSource, packedLight, player);
            poseStack.mulPose(POST_HAND_RENDER_ROT);
            poseStack.translate(-0.5, 0.2, 0.5);
        } else if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(0, -0.5, -0.2);
            poseStack.mulPose(HAND_XP_ROT);
            poseStack.mulPose(LEFT_HAND_YP_ROT);
            poseStack.mulPose(LEFT_HAND_ZP_ROT);
            playerRenderer.renderLeftHand(poseStack, multiBufferSource, packedLight, player);
            poseStack.mulPose(POST_HAND_RENDER_ROT);
            poseStack.translate(0, 0.5, 0.2);
        }
        poseStack.scale(0.5f, 0.5f, 0.5f);
    }
}
