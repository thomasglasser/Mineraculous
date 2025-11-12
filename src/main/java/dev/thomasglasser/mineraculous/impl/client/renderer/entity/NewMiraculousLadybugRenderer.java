package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.NewMiraculousLadybug;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class NewMiraculousLadybugRenderer extends EntityRenderer<NewMiraculousLadybug> {
    private ArrayList<TexturedOutlinedQuad> texturedOutlinedQuads = new ArrayList<>();
    private ArrayList<TailPoint> tailPoints = new ArrayList<>();

    public NewMiraculousLadybugRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NewMiraculousLadybug entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        System.out.println("before if");
        float splinePosition = entity.getSplinePosition();
        double splineParameter = Mth.lerp(partialTick, entity.getOldSplinePosition(), splinePosition);
        MineraculousMathUtils.CatmullRom path = entity.getPath();
        if (path != null && splineParameter >= path.getFirstParameter() && splineParameter < path.getLastParameter() - 0.1d) {
            Vec3 interpolatedPos = entity.getPosition(0); //TODO check if actual interp is needed
            updateTailPoints(path, splineParameter, interpolatedPos, entity.getDistanceToNearestBlockTarget());
            spawnLadybugs();
            updateLadybugs();
            renderLadybugs(bufferSource, poseStack);
            texturedOutlinedQuads.removeIf(ladybug -> ladybug.life <= 0);

        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void quad(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float u1, float u2, float v,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4) {
        MineraculousClientUtils.vertex(vertexConsumer, pose, x1, y1, z1, u1, v, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, x2, y2, z2, u1, v, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, x3, y3, z3, u2, v, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, x4, y4, z4, u2, v, light);
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
            double deltaSeconds = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() / SharedConstants.TICKS_PER_SECOND;
            double toSpawn = spawnRate * deltaSeconds;
            for (int j = 1; j <= toSpawn; j++) summonMagicLadybug(tailPoint.position);
            if (Math.random() < (toSpawn % 1)) summonMagicLadybug(tailPoint.position);
        }
    }

    private void updateLadybugs() {
        Random random = new Random();

        for (TexturedOutlinedQuad ladybug : texturedOutlinedQuads) {
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

    private void constrainLadybug(TexturedOutlinedQuad ladybug, TailPoint nearest, TailPoint secondNearest) {
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

    private static void applyMotion(TexturedOutlinedQuad ladybug, Random random, Vec3 forward, Vec3 upward, Vec3 sideway) {
        double shakeStrength = MineraculousClientConfig.get().shakeStrength.get() / 100f;
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
        int lbLifetime = MineraculousClientConfig.get().lifetime.getAsInt();
        double x = Math.random() * 9 - 4.5;
        double y = Math.random() * 9 - 4.5;
        double z = Math.random() * 9 - 4.5;
        Vec3 spawnPos = new Vec3(x, y, z).add(position);

        TexturedOutlinedQuad ladybug = new TexturedOutlinedQuad(spawnPos, size, lbLifetime);
        texturedOutlinedQuads.add(ladybug);
    }

    private void renderLadybugs(MultiBufferSource multiBufferSource, PoseStack poseStack) {
        HashMap<TexturedOutlinedQuad, Double> rotations = new HashMap<>();
        for (TexturedOutlinedQuad ladybug : texturedOutlinedQuads) {
            rotations.computeIfAbsent(ladybug, (k) -> Math.random() * 360);
        }

        for (TexturedOutlinedQuad ladybug : texturedOutlinedQuads) {
            ladybug.renderOutline(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
        }

        for (TexturedOutlinedQuad ladybug : texturedOutlinedQuads) {
            ladybug.renderBody(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(NewMiraculousLadybug entity) {
        return MineraculousConstants.EMPTY_TEXTURE;
    }

    private static class TexturedOutlinedQuad {
        private Vec3 pos; // relative to the entity
        private final double size;
        private double life;

        private TexturedOutlinedQuad(Vec3 pos, double size, double life) {
            this.pos = pos;
            this.size = size;
            this.life = life;
        }

        private void move(Vec3 vec) {
            this.pos = pos.add(vec);
        }

        private void renderOutline(MultiBufferSource multiBufferSource, PoseStack poseStack, double degrees) {
            MineraculousClientUtils.rotateFacingCamera(poseStack, pos, degrees);
            VertexConsumer ladybug_outline = multiBufferSource.getBuffer(MineraculousRenderTypes.LADYBUG_OUTLINE);
            double quadSize = size * 0.47 / 0.4;
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(-quadSize, quadSize, 0), 0, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(quadSize, quadSize, 0), 1, 0, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(quadSize, -quadSize, 0), 1, 1, LightTexture.FULL_BRIGHT);
            MineraculousClientUtils.vertex(ladybug_outline, poseStack.last(), pos.add(-quadSize, -quadSize, 0), 0, 1, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        private void renderBody(MultiBufferSource multiBufferSource, PoseStack poseStack, double degrees) {
            MineraculousClientUtils.rotateFacingCamera(poseStack, pos, degrees);
            VertexConsumer ladybug = multiBufferSource.getBuffer(MineraculousRenderTypes.LADYBUG_BODY);
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
