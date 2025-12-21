package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class YoyoRopeRenderer {
    public static final ResourceLocation ROPE_TEXTURE = MineraculousConstants.modLoc("textures/item/ladybug_yoyo_rope.png");

    private static final int POINTS = 100;
    private static final double CATENARY_CURVE_FACTOR = 2048.0;
    private static final double ROPE_THICKNESS = 0.025;

    public static void render(Entity entity, Player ropeOwner, double maxLength, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        poseStack.pushPose();

        Vec3 playerHandPos;

        boolean isFirstPerson = ropeOwner == Minecraft.getInstance().player &&
                Minecraft.getInstance().options.getCameraType().isFirstPerson();

        if (isFirstPerson) {
            playerHandPos = MineraculousClientUtils.getFirstPersonHandPosition(false, partialTick);
        } else {
            playerHandPos = MineraculousClientUtils.getHumanoidEntityHandPos(ropeOwner, false, partialTick, 0.15f, -0.75, 0.35f);
        }

        Vec3 entityPos = entity.getPosition(partialTick);
        double offset = entity.getBbHeight() / 2;
        entityPos = entityPos.add(0, offset, 0);
        poseStack.translate(0, offset, 0);
        renderRope(playerHandPos, entityPos, maxLength, poseStack, bufferSource);
        poseStack.popPose();
    }

    public static void renderRope(Vec3 playerHandPos, Vec3 projectilePos, double maxLength, PoseStack poseStack, MultiBufferSource bufferSource) {
        final List<RopePoint> points = new ObjectArrayList<>();
        Vec3 lastProjectilePos = Vec3.ZERO;
        Vec3 lastPlayerHandPos = Vec3.ZERO;
        double lastMaxLength = 0;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        Vec3 fromProjectileToHand = new Vec3(playerHandPos.x - projectilePos.x, playerHandPos.y - projectilePos.y, playerHandPos.z - projectilePos.z);
        Vec3 ropeThickness = getSegmentThickness(projectilePos, playerHandPos);

        double length = fromProjectileToHand.length();
        if (length >= maxLength - 1.5) {
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) -ropeThickness.x, (float) -ropeThickness.y, (float) -ropeThickness.z, 0f, 1f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x - ropeThickness.x), (float) (fromProjectileToHand.y - ropeThickness.y), (float) (fromProjectileToHand.z - ropeThickness.z), 1f, 1f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x + ropeThickness.x), (float) (fromProjectileToHand.y + ropeThickness.y), (float) (fromProjectileToHand.z + ropeThickness.z), 1f, 0f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) ropeThickness.x, (float) +ropeThickness.y, (float) ropeThickness.z, 0f, 0f, LightTexture.FULL_BRIGHT);
        } else {
            calculateRopePoints(points, lastProjectilePos, lastPlayerHandPos, lastMaxLength, projectilePos, playerHandPos, maxLength);

            Vec3 firstPointPos = new Vec3(points.getFirst().worldX() + projectilePos.x, points.getFirst().localY() + projectilePos.y, points.getFirst().worldZ() + projectilePos.z);
            Vec3 projectileToFirstPointThickness = getSegmentThickness(projectilePos, firstPointPos);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (-projectileToFirstPointThickness.x), (float) (-projectileToFirstPointThickness.y), (float) (-projectileToFirstPointThickness.z), 0f, 1f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (points.getFirst().worldX() - projectileToFirstPointThickness.x), (float) (points.getFirst().localY() - projectileToFirstPointThickness.y), (float) (points.getFirst().worldZ() - projectileToFirstPointThickness.z), 1f, 1f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (points.getFirst().worldX() + projectileToFirstPointThickness.x), (float) (points.getFirst().localY() + projectileToFirstPointThickness.y), (float) (points.getFirst().worldZ() + projectileToFirstPointThickness.z), 1f, 0f, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(vertexConsumer, pose, (float) (projectileToFirstPointThickness.x), (float) (projectileToFirstPointThickness.y), (float) (projectileToFirstPointThickness.z), 0f, 0f, LightTexture.FULL_BRIGHT);

            for (int i = 0; i < POINTS - 1; i++) {
                RopePoint pA = points.get(i);
                RopePoint pB = points.get(i + 1);

                Vec3 pointPos = new Vec3(
                        pA.worldX() + projectilePos.x,
                        pA.localY() + projectilePos.y,
                        pA.worldZ() + projectilePos.z);
                Vec3 nextPointPos = new Vec3(
                        pB.worldX() + projectilePos.x,
                        pB.localY() + projectilePos.y,
                        pB.worldZ() + projectilePos.z);

                Vec3 dir = nextPointPos.subtract(pointPos).normalize().scale(0.002);
                Vec3 p1 = pointPos.subtract(dir);
                Vec3 p2 = nextPointPos.add(dir);

                double p1x = p1.x - projectilePos.x;
                double p1y = p1.y - projectilePos.y;
                double p1z = p1.z - projectilePos.z;

                double p2x = p2.x - projectilePos.x;
                double p2y = p2.y - projectilePos.y;
                double p2z = p2.z - projectilePos.z;

                Vec3 thickness = getSegmentThickness(p1, p2);
                MineraculousClientUtils.vertex(vertexConsumer, pose,
                        (float) (p1x - thickness.x), (float) (p1y - thickness.y), (float) (p1z - thickness.z),
                        0f, 1f, LightTexture.FULL_BRIGHT);

                MineraculousClientUtils.vertex(vertexConsumer, pose,
                        (float) (p2x - thickness.x), (float) (p2y - thickness.y), (float) (p2z - thickness.z),
                        1f, 1f, LightTexture.FULL_BRIGHT);

                MineraculousClientUtils.vertex(vertexConsumer, pose,
                        (float) (p2x + thickness.x), (float) (p2y + thickness.y), (float) (p2z + thickness.z),
                        1f, 0f, LightTexture.FULL_BRIGHT);

                MineraculousClientUtils.vertex(vertexConsumer, pose,
                        (float) (p1x + thickness.x), (float) (p1y + thickness.y), (float) (p1z + thickness.z),
                        0f, 0f, LightTexture.FULL_BRIGHT);
            }
        }
    }

    private static void calculateRopePoints(List<RopePoint> points, Vec3 lastProjectilePos, Vec3 lastPlayerHandPos, double lastMaxLength, Vec3 projectilePos, Vec3 playerHandPos, double maxLength) {
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
        }
    }

    private static boolean shouldRecalculatePoints(Vec3 projectilePos, Vec3 lastProjectilePos, Vec3 lastPlayerHandPos, double lastMaxLength, Vec3 playerHandPos, double maxLength) {
        return lastProjectilePos == null
                || lastPlayerHandPos == null
                || !lastProjectilePos.equals(projectilePos)
                || !lastPlayerHandPos.equals(playerHandPos)
                || lastMaxLength != maxLength;
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
        segmentThinkness = segmentThinkness.normalize().scale(ROPE_THICKNESS);
        return segmentThinkness;
    }
}
