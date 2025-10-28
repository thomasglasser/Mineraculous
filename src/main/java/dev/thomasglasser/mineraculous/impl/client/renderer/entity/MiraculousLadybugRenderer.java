package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MiraculousLadybugRenderer extends EntityRenderer<MiraculousLadybug> {
    private ArrayList<MagicLadybug> magicLadybugs = new ArrayList<>();
    private ArrayList<TailPoint> tailPoints = new ArrayList<>();

    private static final ResourceLocation LADYBUG_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug.png");
    private static final ResourceLocation LADYBUG_OUTLINE_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug_glow.png");

    public MiraculousLadybugRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MiraculousLadybug entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        MiraculousLadybugTargetData targetData = entity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET);
        double splineParameter = Mth.lerp(partialTick, entity.oldSplinePosition, targetData.splinePosition());
        if (entity.path instanceof MineraculousMathUtils.CatmullRom path && splineParameter >= path.getFirstParameter()) {
            Vec3 interpolatedPos = MineraculousClientUtils.getInterpolatedPos(entity, partialTick);
            updateTailPoints(path, splineParameter, interpolatedPos, entity.getDistanceToNearestBlockTarget());
            spawnLadybugs();
            updateLadybugs();
            renderLadybugs(bufferSource, poseStack);
            magicLadybugs.removeIf(ladybug -> ladybug.life <= 0);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void updateTailPoints(MineraculousMathUtils.CatmullRom path, double positionParameter, Vec3 interpolatedPos, double distance) {
        double factor = 1 - Math.clamp((distance - 10) / 10d, 0, 1);
        double tailPointRadius = Mth.lerp(factor, 0.4, 1.3); //1.3 meant for teardrop and 0.4 meant for slimmer version
        int tailPointsMaxCount = 7;
        for (int i = 0; i < tailPointsMaxCount; i++) {
            Vec3 tailPointPosition = path.getPoint(path.getParameterBehind(positionParameter, i)).subtract(interpolatedPos);
            if (tailPoints.size() >= i + 1) {
                tailPoints.set(i, new TailPoint(tailPointPosition, tailPointRadius));
            } else {
                tailPoints.add(new TailPoint(tailPointPosition, tailPointRadius));
            }
            tailPointRadius *= 0.75;
        }
        while (tailPoints.size() > tailPointsMaxCount) tailPoints.removeFirst();
    }

    private void spawnLadybugs() {
        for (int i = 0; i < tailPoints.size(); i++) {
            TailPoint tailPoint = tailPoints.get(i);
            if (i > 0) {
                tailPoint.position = tailPoint.position
                        .subtract(tailPoints.get(i - 1).position)
                        .normalize()
                        .add(tailPoints.get(i - 1).position);
            }
            double spawnRate = 100;
            double deltaSeconds = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() / 20.0;
            double toSpawn = spawnRate * deltaSeconds;
            for (int j = 1; j <= toSpawn; j++) summonMagicLadybug(tailPoint.position);
            if (Math.random() < (toSpawn % 1)) summonMagicLadybug(tailPoint.position);
        }
    }

    private void updateLadybugs() {
        Random random = new Random();

        for (MagicLadybug ladybug : magicLadybugs) {
            // Get nearest and second-nearest tail points
            Pair<TailPoint, TailPoint> nearPoints = findNearestTwoPoints(ladybug.pos, tailPoints);
            TailPoint nearest = nearPoints.first;
            TailPoint secondNearest = nearPoints.second;

            // Compute directions
            Vec3[] computedDirections = computeBasisVectors(nearest, secondNearest);
            Vec3 forward = computedDirections[0];
            Vec3 sideway = computedDirections[1];
            Vec3 upward = computedDirections[2];

            // Apply shaking and backwards movement
            applyMotion(ladybug, random, forward, upward, sideway);

            // Constrain position
            constrainLadybug(ladybug, nearest, secondNearest);
        }
    }

    private void constrainLadybug(MagicLadybug ladybug, TailPoint nearest, TailPoint secondNearest) {
        Vec3 position = ladybug.pos;
        if (secondNearest == null) {
            if (position.subtract(nearest.position).length() > nearest.radius) {
                ladybug.pos = position.subtract(nearest.position).normalize().scale(nearest.radius);
            }
        } else {
            Vec3 ab = secondNearest.position.subtract(nearest.position);
            double abDot = ab.dot(ab); // squared length of AB

            double q = 0.0;
            if (abDot > 1e-8) { // avoid divide by zero
                q = position.subtract(nearest.position).dot(ab) / abDot;
                q = Math.max(0.0, Math.min(1.0, q)); // clamp to segment
            }

            // closest point on the segment AB to p
            Vec3 closest = nearest.position.add(ab.scale(q));
            Vec3 secondClosest = secondNearest.position.add(ab.scale(q));

            // interpolate radius between the two tail points
            double interpRadius = nearest.radius * (1.0 - q) + secondNearest.radius * q;

            double dist = position.subtract(closest).length();
            double dist2 = position.subtract(secondClosest).length();

            if (nearest == tailPoints.getLast() && dist2 > 1) {
                ladybug.life = 0;
            }

            if (dist > interpRadius) {
                Vec3 dir = position.subtract(closest).normalize();
                ladybug.pos = closest.add(dir.scale(Math.random() * 2 * interpRadius - interpRadius));
            }
        }
    }

    private static void applyMotion(MagicLadybug ladybug, Random random, Vec3 forward, Vec3 upward, Vec3 sideway) {
        double shakeStrength = MineraculousClientConfig.get().magicLadybugsShakeStrength.get() / 100f;
        double dx = -0.03; // slowly slide backwards
        double dy = (random.nextDouble() - 0.5) * shakeStrength;
        double dz = (random.nextDouble() - 0.5) * shakeStrength;
        ladybug.move(forward.scale(dx)
                .add(upward.scale(dy))
                .add(sideway.scale(dz)));
    }

    private static Pair<TailPoint, TailPoint> findNearestTwoPoints(Vec3 pos, ArrayList<TailPoint> tailPoints) {
        TailPoint nearestPoint = null;
        TailPoint secondNearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        double secondMinDistance = Double.MAX_VALUE;

        for (TailPoint tailPoint : tailPoints) {
            double distance = pos.subtract(tailPoint.position).length();
            if (distance < minDistance) {
                secondMinDistance = minDistance;
                secondNearestPoint = nearestPoint;

                minDistance = distance;
                nearestPoint = tailPoint;
            } else if (distance < secondMinDistance) {
                secondMinDistance = distance;
                secondNearestPoint = tailPoint;
            }
        }
        return Pair.of(nearestPoint, secondNearestPoint);
    }

    private static Vec3[] computeBasisVectors(TailPoint nearest, TailPoint secondNearest) {
        Vec3 forward = nearest.position.subtract(secondNearest.position).normalize();
        forward = nearest.position.length() < secondNearest.position.length() ? forward : forward.scale(-1); // pointing to the head
        Vec3 sideway = forward.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 upward = sideway.cross(forward).normalize();

        return new Vec3[] { forward, sideway, upward };
    }

    private void summonMagicLadybug(Vec3 position) {
        double size = 0.1 + (Math.random() * (0.3 - 0.1));
        int lbLifetime = MineraculousClientConfig.get().magicLadybugsLifetime.getAsInt();
        double x = Math.random() * 9 - 4.5;
        double y = Math.random() * 9 - 4.5;
        double z = Math.random() * 9 - 4.5;
        Vec3 spawnPos = new Vec3(x, y, z).add(position);

        MagicLadybug ladybug = new MagicLadybug(spawnPos, size, lbLifetime);
        magicLadybugs.add(ladybug);
    }

    private void renderLadybugs(MultiBufferSource multiBufferSource, PoseStack poseStack) {
        HashMap<MagicLadybug, Double> rotations = new HashMap<>();
        for (MagicLadybug ladybug : magicLadybugs) {
            rotations.computeIfAbsent(ladybug, (k) -> Math.random() * 360);
        }

        for (MagicLadybug ladybug : magicLadybugs) {
            ladybug.renderOutline(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
        }

        for (MagicLadybug ladybug : magicLadybugs) {
            ladybug.renderBody(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousLadybug entity) {
        return MineraculousConstants.modLoc("textures/item/empty.png");
    }

    private static class MagicLadybug {
        private Vec3 pos; // relative to the entity
        private final double size;
        private double life;

        private final RenderType LADYBUG = MineraculousRenderTypes.magicLadybugBody(LADYBUG_TEXTURE);
        private final RenderType OUTLINE = MineraculousRenderTypes.magicLadybugOutline(LADYBUG_OUTLINE_TEXTURE);

        private MagicLadybug(Vec3 pos, double size, double life) {
            this.pos = pos;
            this.size = size;
            this.life = life;
        }

        private void move(Vec3 vec) {
            this.pos = pos.add(vec);
        }

        private void renderOutline(MultiBufferSource multiBufferSource, PoseStack poseStack, double degrees) {
            MineraculousClientUtils.rotateFacingCamera(poseStack, pos, degrees);
            VertexConsumer ladybug_outline = multiBufferSource.getBuffer(OUTLINE);
            double quadSize = size * 0.47 / 0.4;
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(-quadSize, quadSize, 0), 0, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(quadSize, quadSize, 0), 1, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(quadSize, -quadSize, 0), 1, 1, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(-quadSize, -quadSize, 0), 0, 1, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        private void renderBody(MultiBufferSource multiBufferSource, PoseStack poseStack, double degrees) {
            MineraculousClientUtils.rotateFacingCamera(poseStack, pos, degrees);
            VertexConsumer ladybug = multiBufferSource.getBuffer(LADYBUG);
            double quadSize = size;
            MineraculousClientUtils.vertex(ladybug, poseStack.last(), pos.add(-quadSize, quadSize, 0), 0, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug, poseStack.last(), pos.add(quadSize, quadSize, 0), 1, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug, poseStack.last(), pos.add(quadSize, -quadSize, 0), 1, 1, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug, poseStack.last(), pos.add(-quadSize, -quadSize, 0), 0, 1, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            if (life > 0) life--;
        }
    }

    private static class TailPoint {
        private final double radius;
        private Vec3 position;

        TailPoint(Vec3 pos, double r) {
            radius = r;
            position = pos;
        }
    }
}
