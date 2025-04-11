package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class LadybugYoyoRenderer extends BlockingGeoItemRenderer<LadybugYoyoItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo.png");

    private static final ResourceLocation SHIELD_TEXTURE = Mineraculous.modLoc("textures/item/geo/ladybug_yoyo_shield.png");

    //private static final DefaultedItemGeoModel<LadybugYoyoItem> = DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo"));

    public LadybugYoyoRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")), new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo_shield")));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Player player = Minecraft.getInstance().player;
        MultiBufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        if (player == Minecraft.getInstance().player && stack != null && player != null) {
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (stack.is(MineraculousItems.LADYBUG_YOYO) && player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO).isPresent()) {
                HumanoidArm arm = HumanoidArm.RIGHT;
                if (player.getMainHandItem() == stack) {
                    arm = player.getMainArm();
                } else if (player.getOffhandItem() == stack) {
                    arm = player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
                }
                renderHand(entityRenderDispatcher, player, arm, poseStack, multiBufferSource, packedLight);
            }
        }
        super.renderByItem(stack, transform, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private static void renderHand(EntityRenderDispatcher entityRenderDispatcher, Player player, HumanoidArm side, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player && entityRenderDispatcher.camera != null) {
            PlayerRenderer playerRenderer = (PlayerRenderer) entityRenderDispatcher.getRenderer(Minecraft.getInstance().player);
            if (side == HumanoidArm.RIGHT) {
                poseStack.scale(2, 2, 2);
                poseStack.translate(0.5, -0.5, -0.2);
                poseStack.mulPose(Axis.XP.rotationDegrees(-40));
                poseStack.mulPose(Axis.YP.rotationDegrees(110));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-10));
                playerRenderer.renderRightHand(poseStack, multiBufferSource, packedLight, Minecraft.getInstance().player);
                poseStack.mulPose(new Quaternionf(new AxisAngle4d(-1, -01.f, 0.3f, 0.1f)));
                poseStack.translate(-0.5, 0.2, +0.5);
                poseStack.scale(0.5f, 0.5f, 0.5f);
            } else {
                poseStack.scale(2, 2, 2);
                poseStack.translate(0, -0.5, -0.2);
                poseStack.mulPose(Axis.XP.rotationDegrees(-40));
                poseStack.mulPose(Axis.YP.rotationDegrees(-110));
                poseStack.mulPose(Axis.ZP.rotationDegrees(10));
                playerRenderer.renderLeftHand(poseStack, multiBufferSource, packedLight, Minecraft.getInstance().player);
                poseStack.mulPose(new Quaternionf(new AxisAngle4d(-1, -01.f, 0.3f, 0.1f)));
                poseStack.translate(0, 0.5, 0.2);
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(LadybugYoyoItem animatable) {
        if (isBlocking())
            return SHIELD_TEXTURE;
        return TEXTURE;
    }
}
