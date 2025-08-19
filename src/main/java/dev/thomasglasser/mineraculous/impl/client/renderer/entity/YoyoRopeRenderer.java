package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class YoyoRopeRenderer {
    public static final ResourceLocation ROPE_TEXTURE = Mineraculous.modLoc("textures/item/ladybug_yoyo_rope.png");

    private static final int POINTS = 100;
    private static final double CATENARY_CURVE_FACTOR = 2048.0;
    public static final float RIGHT_SCALE = 0.55f;
    public static final float UP_SCALE = -0.6f;

    public static void render(LivingEntity entity, Player ropeOwner, double maxLength, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        Vec3 playerHandPos;
        if (ropeOwner == Minecraft.getInstance().player && Minecraft.getInstance().getEntityRenderDispatcher().options.getCameraType().isFirstPerson()) {
            playerHandPos = MineraculousClientUtils.getFirstPersonHandPosition(false, false, partialTick, RIGHT_SCALE, UP_SCALE);
        } else {
            playerHandPos = MineraculousClientUtils.getHumanoidEntityHandPos(ropeOwner, false, partialTick, 0.15f, -0.75, 0.35f);
        }
        Vec3 entityPos = entity.getPosition(partialTick);
        renderRope(playerHandPos, entityPos, maxLength, poseStack, bufferSource);
    }

    public static void renderRope(Vec3 playerHandPos, Vec3 projectilePos, double maxLength, PoseStack poseStack, MultiBufferSource bufferSource) {
        final List<RopePoint> points = new ReferenceArrayList<>();
        Vec3 lastProjectilePos = Vec3.ZERO;
        Vec3 lastPlayerHandPos = Vec3.ZERO;
        double lastMaxLength = 0;

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
            List<RopePoint> pointList = calculateRopePoints(points, lastProjectilePos, lastPlayerHandPos, lastMaxLength, projectilePos, playerHandPos, maxLength);

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
    }

    private static List<RopePoint> calculateRopePoints(List<RopePoint> points, Vec3 lastProjectilePos, Vec3 lastPlayerHandPos, double lastMaxLength, Vec3 projectilePos, Vec3 playerHandPos, double maxLength) {
        if (shouldRecalculatePoints(projectilePos, lastProjectilePos, lastPlayerHandPos, lastMaxLength, playerHandPos, maxLength)) {
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

    private static boolean shouldRecalculatePoints(Vec3 projectilePos, Vec3 lastProjectilePos, Vec3 lastPlayerHandPos, double lastMaxLength, Vec3 playerHandPos, double maxLength) {
        return lastProjectilePos == null
                || lastPlayerHandPos == null
                || !lastProjectilePos.equals(projectilePos)
                || !lastPlayerHandPos.equals(playerHandPos)
                || lastMaxLength != maxLength;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    private static Vec3 getSegmentThickness(Vec3 projectilePos, Vec3 handPos) {
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
