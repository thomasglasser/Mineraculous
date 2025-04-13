package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class LadybugYoyoRenderer extends BlockingGeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo.png");

    private static final ResourceLocation SHIELD_TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo_shield.png");

    // Hand Rendering Constants
    private static final Quaternionf HAND_XP_ROT = Axis.XP.rotationDegrees(-40);
    private static final Quaternionf RIGHT_HAND_YP_ROT = Axis.YP.rotationDegrees(110);
    private static final Quaternionf LEFT_HAND_YP_ROT = Axis.YP.rotationDegrees(-110);
    private static final Quaternionf RIGHT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(-10);
    private static final Quaternionf LEFT_HAND_ZP_ROT = Axis.ZP.rotationDegrees(10);
    private static final Quaternionf POST_HAND_RENDER_ROT = new Quaternionf(new AxisAngle4d(-1, -1, 0.3f, 0.1f));

    public LadybugYoyoRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")), new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo_shield")));
    }

    @Override
    public void defaultRender(PoseStack poseStack, LadybugYoyoItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (this.renderPerspective.firstPerson() && player != null && player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO).isPresent())
            renderHand(player, this.renderPerspective, poseStack, bufferSource, packedLight);
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

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        if (isBlocking())
            return SHIELD_TEXTURE;
        return TEXTURE;
    }
}
