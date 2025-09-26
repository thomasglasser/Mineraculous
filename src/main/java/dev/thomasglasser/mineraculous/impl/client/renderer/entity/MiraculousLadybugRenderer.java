package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class MiraculousLadybugRenderer extends EntityRenderer<MiraculousLadybug> {
    private ArrayList<MagicLadybug> magicLadybugs = new ArrayList<>();
    private ArrayList<TailPoint> tailPoints = new ArrayList<>();
    private Quaternionf smoothedRotation = new Quaternionf();

    //TODO add the assets
    private static final ResourceLocation LADYBUG_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug.png");
    private static final ResourceLocation LADYBUG_OUTLINE_TEXTURE = MineraculousConstants.modLoc("textures/particle/ladybug_glow.png");
    private static final int LADYBUG_DENSITY = 10;

    public MiraculousLadybugRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MiraculousLadybug entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        double t = entity.t;
        Vec3 interpolatedPos = new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        /*if (entity.path != null) {
            MiraculousLadybug.CatmullRom path = entity.path;
            if (t >= path.getFirstParameter()) {
                double rad = 0.4;
                for (int i = 0; i < 7; i++) {
                    Vec3 position = path.getPoint(path.getParameterBehind(t, i)).subtract(interpolatedPos);
                    if (tailPoints.size() >= i + 1) {
                        tailPoints.set(i, new TailPoint(position, rad));
                    } else {
                        tailPoints.add(new TailPoint(position, rad));
                    }
                    rad *= 0.8;
                }
        
                while (tailPoints.size() > 7) {
                    tailPoints.removeFirst();
                }
        
                for (int i = 0; i < tailPoints.size(); i++) {
                    TailPoint tailPoint = tailPoints.get(i);
                    if (i > 0) {
                        tailPoint.position = tailPoint.position.subtract(tailPoints.get(i - 1).position).normalize().add(tailPoints.get(i - 1).position);
                    }
                    double spawnRate = 100;
                    double deltaSeconds = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() / 20.0;
                    double toSpawn = spawnRate * deltaSeconds;
                    for (int j = 1; j <= toSpawn; j++) {
                        summonMagicLadybug(tailPoint.position);
                    }
                    if (Math.random() < (toSpawn % 1)) {
                        summonMagicLadybug(tailPoint.position);
                    }
                }
        
                Random random = new Random();
        
                for (MagicLadybug ladybug : magicLadybugs) {
                    // Get nearest and second-nearest tail points
                    TailPoint nearestPoint = null;
                    TailPoint secondNearestPoint = null;
                    double minDistance = Double.MAX_VALUE;
                    double secondMinDistance = Double.MAX_VALUE;
        
                    for (int i = 0; i < tailPoints.size(); i++) {
                        TailPoint tailPoint = tailPoints.get(i);
                        double distance = ladybug.pos.subtract(tailPoint.position).length();
        
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
        
                    // Compute directions
                    Vec3 forward = nearestPoint.position.subtract(secondNearestPoint.position).normalize();
                    forward = nearestPoint.position.length() < secondNearestPoint.position.length() ? forward : forward.scale(-1); // pointing to the head
                    Vec3 sideway = forward.cross(new Vec3(0, 1, 0)).normalize();
                    Vec3 upward = sideway.cross(forward).normalize();
        
                    // Apply shaking and backwards movement
                    double shakeStrength = MineraculousClientConfig.get().magicLadybugsShakeStrength.get() / 100f;
                    double dx = -0.05; // slowly slide backwards
                    double dy = (random.nextDouble() - 0.5) * shakeStrength;
                    double dz = (random.nextDouble() - 0.5) * shakeStrength;
        
                    forward = forward.scale(dx);
                    upward = upward.scale(dy);
                    sideway = sideway.scale(dz);
        
                    ladybug.move(forward.add(upward).add(sideway));
        
                    // Constrain position
                    if (secondNearestPoint == null) {
                        // fallback if we don't have two points
                        if (minDistance > nearestPoint.radius) {
                            ladybug.pos = ladybug.pos.subtract(nearestPoint.position).normalize().scale(nearestPoint.radius);
                        }
                    } else {
                        Vec3 a = nearestPoint.position;
                        Vec3 b = secondNearestPoint.position;
                        Vec3 p = ladybug.pos;
        
                        Vec3 ab = b.subtract(a);
                        double abDot = ab.dot(ab); // squared length of AB
        
                        double q = 0.0;
                        if (abDot > 1e-8) { // avoid divide by zero
                            q = p.subtract(a).dot(ab) / abDot;
                            q = Math.max(0.0, Math.min(1.0, q)); // clamp to segment
                        }
        
                        // closest point on the segment AB to p
                        Vec3 closest = a.add(ab.scale(q));
        
                        // interpolate radius between the two tail points
                        double interpRadius = nearestPoint.radius * (1.0 - q) + secondNearestPoint.radius * q;
        
                        double dist = p.subtract(closest).length();
                        if (dist > interpRadius) {
                            Vec3 dir = p.subtract(closest).normalize();
                            ladybug.pos = closest.add(dir.scale(Math.random() * 2 * interpRadius - interpRadius));
                        }
                    }
                }
        
                MagicLadybug.render(magicLadybugs, bufferSource, poseStack, smoothedRotation, partialTick);
            }
        }*/
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void summonMagicLadybug(Vec3 spawnPos) {
        double size = 0.1 + (Math.random() * (0.3 - 0.1));
        int maxLbCount = 30; //TODO CONFIG
        // MineraculousClientConfig.get().magicLadybugsCount.get() * 100;
        double maxLifeTime = (double) maxLbCount / LADYBUG_DENSITY;
        int lbLifetime = (int) (Math.random() * (maxLifeTime - 60) + 60);

        /*double x = Math.random() * 9 - 4.5;
        double y = Math.random() * 9 - 4.5;
        double z = Math.random() * 9 - 4.5;
        
        Vec3 spawnPos = new Vec3(x, y, z);*/
        MagicLadybug ladybug = new MagicLadybug(spawnPos, size, lbLifetime);
        magicLadybugs.add(ladybug);
    }

    private void initialMagicLadybugs(Vec3 lookVec) {
        Vec3 forward = lookVec;
        Vec3 sideway = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 upward = sideway.cross(lookVec).normalize();
        forward = forward.scale(Math.random() * 5);
        sideway = sideway.scale(Math.random() * 5 - 2.5);
        upward = upward.scale(Math.random() * 5 - 2.5);

        Vec3 spawnPos = forward.add(sideway).add(upward);
        summonMagicLadybug(spawnPos);
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

        private void render(MultiBufferSource multiBufferSource, PoseStack poseStack, double degrees) {
            MineraculousClientUtils.rotateFacingCamera(poseStack, pos, degrees);
            VertexConsumer ladybug = multiBufferSource.getBuffer(LADYBUG);
            double quadSize = size;
            float tint = 1f;
            ladybug.addVertex(poseStack.last(), pos.add(-quadSize, quadSize, 0).toVector3f())
                    .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), pos.add(quadSize, quadSize, 0).toVector3f())
                    .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), pos.add(quadSize, -quadSize, 0).toVector3f())
                    .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), pos.add(-quadSize, -quadSize, 0).toVector3f())
                    .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            poseStack.popPose();

            if (life > 0) life--;
        }

        private static void render(ArrayList<MagicLadybug> magicLadybugs, MultiBufferSource multiBufferSource, PoseStack poseStack, Quaternionf rotation, float partialTick) {
            HashMap<MagicLadybug, Double> rotations = new HashMap<>();
            for (MagicLadybug ladybug : magicLadybugs) {
                rotations.computeIfAbsent(ladybug, (k) -> Math.random() * 360);
            }

            for (MagicLadybug ladybug : magicLadybugs) {
                ladybug.renderOutline(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
            }

            for (MagicLadybug ladybug : magicLadybugs) {
                ladybug.render(multiBufferSource, poseStack, rotations.getOrDefault(ladybug, 0d));
            }
            magicLadybugs.removeIf(ladybug -> ladybug.life <= 0);
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
