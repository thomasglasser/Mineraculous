package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.client.renderer.item.DefaultedGeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ThrownLadybugYoyoRenderer extends GeoEntityRenderer<ThrownLadybugYoyo> {
    public static final ResourceLocation ROPE_TEXTURE = Mineraculous.modLoc("textures/item/ladybug_yoyo_rope.png");

    private static final ResourceLocation TEXTURE = DefaultedGeoItemRenderer.makeTextureLocation(MineraculousItems.LADYBUG_YOYO.getId());

    public static final float RIGHT_SCALE = 0.55f;
    public static final float UP_SCALE = -0.6f;

    public ThrownLadybugYoyoRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")));
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownLadybugYoyo animatable) {
        return TEXTURE;
    }

    @Override
    public void defaultRender(PoseStack poseStack, ThrownLadybugYoyo animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        if (animatable.getOwner() instanceof Player) {
            poseStack.pushPose();

            Player projectilePlayer = animatable.getPlayerOwner();
            if (projectilePlayer == null) return;
            double maxLength;
            Vec3 playerHandPos;
            boolean offHand = !(animatable.getHand() == InteractionHand.MAIN_HAND);
            if (projectilePlayer == Minecraft.getInstance().player && Minecraft.getInstance().getEntityRenderDispatcher().options.getCameraType().isFirstPerson()) {
                playerHandPos = MineraculousClientUtils.getFirstPersonHandPosition(offHand, false, partialTick, RIGHT_SCALE, UP_SCALE);
                maxLength = animatable.getRenderMaxRopeLength(true);
            } else {
                playerHandPos = MineraculousClientUtils.getHumanoidEntityHandPos(projectilePlayer, offHand, partialTick, 0.15f, -0.75, 0.35f);
                maxLength = animatable.getRenderMaxRopeLength(false);
            }
            Vec3 projectilePos = animatable.getPosition(partialTick);

            YoyoRopeRenderer.renderRope(playerHandPos, projectilePos, maxLength, poseStack, bufferSource);

            if (animatable.getInitialDirection() == Direction.SOUTH || animatable.getInitialDirection() == Direction.NORTH) {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                poseStack.translate(-0.15, 0, 0);
            } else {
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
                poseStack.translate(0, 0, 0.15);
            }
            poseStack.translate(0, -0.1, 0);

            super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
            poseStack.popPose();
        }
    }
}
