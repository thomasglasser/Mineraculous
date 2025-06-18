package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.client.renderer.item.DefaultedGeoItemRenderer;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

    private static final int POINTS = 100;
    private static final double CATENARY_CURVE_FACTOR = 2048.0;
    public static final float RIGHT_SCALE = 0.55f;
    public static final float UP_SCALE = -0.6f;

    private final List<RopePoint> points = new ReferenceArrayList<>();
    private Vec3 lastProjectilePos;
    private Vec3 lastPlayerHandPos;
    private double lastMaxLength;

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
            renderRope(poseStack, animatable, bufferSource, partialTick);
            super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
            poseStack.popPose();
        }
    }

    private void renderRope(PoseStack poseStack, ThrownLadybugYoyo animatable, MultiBufferSource bufferSource, float partialTick) {
        Player projectilePlayer = animatable.getPlayerOwner();
        if (projectilePlayer == null) {
            return;
        }

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

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        Vec3 fromProjectileToHand = new Vec3(playerHandPos.x - projectilePos.x, playerHandPos.y - projectilePos.y, playerHandPos.z - projectilePos.z);
        Vec3 ropeThickness = getSegmentThickness(projectilePos, playerHandPos);

        double length = fromProjectileToHand.length();
        if (length >= maxLength || maxLength > 50) { // If it's bigger than 50 it starts looking weird.
            vertex(vertexConsumer, pose, (float) -ropeThickness.x, (float) -ropeThickness.y, (float) -ropeThickness.z, 0f, 1f);
            vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x - ropeThickness.x), (float) (fromProjectileToHand.y - ropeThickness.y), (float) (fromProjectileToHand.z - ropeThickness.z), 1f, 1f);
            vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x + ropeThickness.x), (float) (fromProjectileToHand.y + ropeThickness.y), (float) (fromProjectileToHand.z + ropeThickness.z), 1f, 0f);
            vertex(vertexConsumer, pose, (float) ropeThickness.x, (float) +ropeThickness.y, (float) ropeThickness.z, 0f, 0f);
        } else {
            List<RopePoint> pointList = calculateRopePoints(projectilePos, playerHandPos, maxLength);

            Vec3 firstPointPos = new Vec3(pointList.getFirst().worldX() + projectilePos.x, pointList.getFirst().localY() + projectilePos.y, pointList.getFirst().worldZ() + projectilePos.z);
            Vec3 projectileToFirstPointThickness = getSegmentThickness(projectilePos, firstPointPos);
            vertex(vertexConsumer, pose, (float) (-projectileToFirstPointThickness.x), (float) (-projectileToFirstPointThickness.y), (float) (-projectileToFirstPointThickness.z), 0f, 1f);
            vertex(vertexConsumer, pose, (float) (pointList.getFirst().worldX() - projectileToFirstPointThickness.x), (float) (pointList.getFirst().localY() - projectileToFirstPointThickness.y), (float) (pointList.getFirst().worldZ() - projectileToFirstPointThickness.z), 1f, 1f);
            vertex(vertexConsumer, pose, (float) (pointList.getFirst().worldX() + projectileToFirstPointThickness.x), (float) (pointList.getFirst().localY() + projectileToFirstPointThickness.y), (float) (pointList.getFirst().worldZ() + projectileToFirstPointThickness.z), 1f, 0f);
            vertex(vertexConsumer, pose, (float) (projectileToFirstPointThickness.x), (float) (projectileToFirstPointThickness.y), (float) (projectileToFirstPointThickness.z), 0f, 0f);

            for (int i = 0; i < POINTS - 1; i++) {
                Vec3 pointPos = new Vec3(pointList.get(i).worldX() + projectilePos.x, pointList.get(i).localY() + projectilePos.y, pointList.get(i).worldZ() + projectilePos.z);
                Vec3 nextPointPos = new Vec3(pointList.get(i + 1).worldX() + projectilePos.x, pointList.get(i + 1).localY() + projectilePos.y, pointList.get(i + 1).worldZ() + projectilePos.z);
                Vec3 point1ToPoint2Thickness = getSegmentThickness(pointPos, nextPointPos);
                vertex(vertexConsumer, pose, (float) (pointList.get(i).worldX() - point1ToPoint2Thickness.x), (float) (pointList.get(i).localY() - point1ToPoint2Thickness.y), (float) (pointList.get(i).worldZ() - point1ToPoint2Thickness.z), 0f, 1f);
                vertex(vertexConsumer, pose, (float) (pointList.get(i + 1).worldX() - point1ToPoint2Thickness.x), (float) (pointList.get(i + 1).localY() - point1ToPoint2Thickness.y), (float) (pointList.get(i + 1).worldZ() - point1ToPoint2Thickness.z), 1f, 1f);
                vertex(vertexConsumer, pose, (float) (pointList.get(i + 1).worldX() + point1ToPoint2Thickness.x), (float) (pointList.get(i + 1).localY() + point1ToPoint2Thickness.y), (float) (pointList.get(i + 1).worldZ() + point1ToPoint2Thickness.z), 1f, 0f);
                vertex(vertexConsumer, pose, (float) (pointList.get(i).worldX() + point1ToPoint2Thickness.x), (float) (pointList.get(i).localY() + point1ToPoint2Thickness.y), (float) (pointList.get(i).worldZ() + point1ToPoint2Thickness.z), 0f, 0f);
            }
        }
        if (animatable.getInitialDirection() == Direction.SOUTH || animatable.getInitialDirection() == Direction.NORTH) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(90));
            poseStack.translate(-0.15, 0, 0);
        } else {
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.translate(0, 0, 0.15);
        }
        poseStack.translate(0, -0.1, 0);
    }

    private List<RopePoint> calculateRopePoints(Vec3 projectilePos, Vec3 playerHandPos, double maxLength) {
        if (shouldRecalculatePoints(projectilePos, playerHandPos, maxLength)) {
            Vec3 fromProjectileToHand = new Vec3(playerHandPos.x - projectilePos.x,
                    playerHandPos.y - projectilePos.y,
                    playerHandPos.z - projectilePos.z);
            Vec3 fromProjectileToHandOnXZ = new Vec3(fromProjectileToHand.x, 0, fromProjectileToHand.z);

            points.clear();
            double lastX = POINTS * (fromProjectileToHandOnXZ.length()) / (POINTS + 1);
            double lastY = POINTS * (playerHandPos.y - projectilePos.y) / POINTS
                    - (2 * POINTS * (maxLength - POINTS * maxLength / (POINTS + 1)) / CATENARY_CURVE_FACTOR);
            RopePoint lastPoint = new RopePoint(lastX, lastY, fromProjectileToHandOnXZ);

            Vec3 offset = new Vec3(fromProjectileToHand.x - lastPoint.worldX(),
                    fromProjectileToHand.y - lastPoint.localY(),
                    fromProjectileToHand.z - lastPoint.worldZ());
            double offsetLength = offset.length();

            for (int i = 1; i <= POINTS; i++) {
                double x = i * (fromProjectileToHandOnXZ.length() + offsetLength) / (POINTS + 1);
                double y = i * (playerHandPos.y - projectilePos.y) / POINTS
                        - (2 * i * (maxLength - i * maxLength / (POINTS + 1)) / CATENARY_CURVE_FACTOR);
                points.add(new RopePoint(x, y, fromProjectileToHandOnXZ));
            }

            lastProjectilePos = projectilePos;
            lastPlayerHandPos = playerHandPos;
            lastMaxLength = maxLength;
        }

        return points;
    }

    private boolean shouldRecalculatePoints(Vec3 projectilePos, Vec3 playerHandPos, double maxLength) {
        return lastProjectilePos == null
                || lastPlayerHandPos == null
                || !lastProjectilePos.equals(projectilePos)
                || !lastPlayerHandPos.equals(playerHandPos)
                || lastMaxLength != maxLength;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    private Vec3 getSegmentThickness(Vec3 projectilePos, Vec3 handPos) {
        Vec3 fromProjectileToHand = new Vec3(handPos.x - projectilePos.x, handPos.y - projectilePos.y, handPos.z - projectilePos.z);
        Vec3 fromProjectileToHandOnXZ = new Vec3(fromProjectileToHand.x, 0, fromProjectileToHand.z);
        Vec3 watcherEyePos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        Vec3 fromProjectileToPOV = new Vec3(watcherEyePos.x - projectilePos.x, watcherEyePos.y - projectilePos.y, watcherEyePos.z - projectilePos.z);

        double xZGraphLineConst = fromProjectileToHandOnXZ.z / fromProjectileToHandOnXZ.x;
        double xZGraphPerpendicularConst = -1 * fromProjectileToHandOnXZ.x / fromProjectileToHandOnXZ.z;
        double offsetParallelLine = fromProjectileToPOV.z - fromProjectileToPOV.x * xZGraphPerpendicularConst;
        double projectionX = offsetParallelLine / (xZGraphLineConst * xZGraphLineConst + 1);
        double projectionZ = projectionX * xZGraphLineConst;

        Vec3 projectionXZ = new Vec3(projectionX, 0, projectionZ);
        double projectionXZlength = projectionXZ.length();
        if (fromProjectileToHandOnXZ.x >= 0 != projectionXZ.x >= 0) {
            projectionXZlength = projectionXZlength * (-1); // Making negative if needed
        }

        double projectionY = fromProjectileToHand.y * projectionXZlength / fromProjectileToHandOnXZ.length();

        Vec3 fromPOVToRope = new Vec3(projectionX - fromProjectileToPOV.x, projectionY - fromProjectileToPOV.y, projectionZ - fromProjectileToPOV.z);

        Vec3 segmentThinkness = fromPOVToRope.cross(fromProjectileToPOV);
        segmentThinkness = segmentThinkness.scale(1 / segmentThinkness.length());
        segmentThinkness = segmentThinkness.scale(0.02d); //0.02 is the thickness
        return segmentThinkness;
    }
}
