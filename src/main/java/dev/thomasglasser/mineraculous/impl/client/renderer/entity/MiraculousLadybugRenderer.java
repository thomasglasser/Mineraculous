package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import static dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes.ladybugMain;
import static dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes.ladybugOutline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MiraculousLadybugRenderer extends EntityRenderer<MiraculousLadybug> {
    private ArrayList<MagicLadybug> magicLadybugs = new ArrayList<>();
    private Quaternionf smoothedRotation = new Quaternionf();

    public MiraculousLadybugRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static final ResourceLocation LADYBUG_TEXTURE = Mineraculous.modLoc("textures/particle/ladybug.png");
    private static final ResourceLocation LADYBUG_OUTLINE_TEXTURE = Mineraculous.modLoc("textures/particle/ladybug_glow.png");
    private static final int LADYBUG_DENSITY = 10;

    @Override
    public void render(MiraculousLadybug entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 lookVec = entity.getLookAngle().normalize().scale(-1); // direction to point the swarm

        Vec3 base = new Vec3(1, 0, 0);
        Vec3 axis = base.cross(lookVec).normalize();
        double angle = Math.acos(base.dot(lookVec));
        Quaternionf targetRotation = new Quaternionf();
        if (!axis.equals(Vec3.ZERO)) {
            targetRotation = new Quaternionf().fromAxisAngleRad(axis.toVector3f(), (float) angle);
        }

        //Smoothly interpolate instead of snapping
        float smoothingSpeed = 5.0f;
        float deltaTime = partialTick * (1.0f / 20.0f); // convert ticks to seconds
        float t = 1.0f - (float) Math.exp(-smoothingSpeed * deltaTime);
        smoothedRotation.slerp(targetRotation, t);

        int maxLbCount = MineraculousClientConfig.get().magicLadybugsCount.get() * 100;


        //if (entity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET).sphereTicks() == 0) {
            if (magicLadybugs.size() < maxLbCount) {
                for (int i = 1; i <= 10; i++) {
                    summonMagicLadybug();
                }
            }
        //}

        while (magicLadybugs.size() > maxLbCount) {
            magicLadybugs.removeFirst();
        }

        Random random = new Random();

        for (MagicLadybug it : magicLadybugs) {
            if (entity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET).sphereTicks() == 0) {
                it.move(new Vec3(0.1, 0, 0));
            }

            double shakeStrength = MineraculousClientConfig.get().magicLadybugsShakeStrength.get() / 100f;
            double dx = 0;
            double dy = (random.nextDouble() - 0.5) * shakeStrength;
            double dz = (random.nextDouble() - 0.5) * shakeStrength;

            it.move(new Vec3(dx, dy, dz));

            if (entity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET).sphereTicks() == 0) {
                if (it.localPos.y > cometFunction(it.localPos.x))
                    it.localPos = new Vec3(it.localPos.x, cometFunction(it.localPos.x), it.localPos.z);
                if (it.localPos.y < -cometFunction(it.localPos.x))
                    it.localPos = new Vec3(it.localPos.x, -cometFunction(it.localPos.x), it.localPos.z);
                if (it.localPos.z > cometFunction(it.localPos.x))
                    it.localPos = new Vec3(it.localPos.x, it.localPos.y, cometFunction(it.localPos.x));
                if (it.localPos.z < -cometFunction(it.localPos.x))
                    it.localPos = new Vec3(it.localPos.x, it.localPos.y, -cometFunction(it.localPos.x));
            }
        }

        poseStack.translate(-0.5, 0, -0.5);
        MagicLadybug.render(magicLadybugs, bufferSource, poseStack, smoothedRotation, partialTick);
        poseStack.translate(0.5, 0, 0.5);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void summonMagicLadybug() {
        double size = 0.1 + (Math.random() * (0.3 - 0.1));
        int maxLbCount = MineraculousClientConfig.get().magicLadybugsCount.get() * 100;
        int lbLifetime = (int) ((double) maxLbCount / LADYBUG_DENSITY);
        MagicLadybug it = new MagicLadybug(
                new Vec3(Math.random() * 2 - 0.5, Math.random(), Math.random()), size, lbLifetime);

        double min = Math.min(cometFunction(it.localPos.horizontalDistance()), -cometFunction(it.localPos.horizontalDistance()));
        double max = Math.max(cometFunction(it.localPos.horizontalDistance()), -cometFunction(it.localPos.horizontalDistance()));

        double randomY = min + (Math.random() * (max - min));
        double randomZ = min + (Math.random() * (max - min));
        it.localPos = new Vec3(it.localPos.x, randomY, randomZ);
        magicLadybugs.add(it);
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousLadybug entity) {
        return Mineraculous.modLoc("textures/item/empty.png");
    }

    private class MagicLadybug {
        private Vec3 localPos;   // path-space position
        private Vec3 worldPos;   // rotated each frame
        private Vec3 prevLocalPos;
        private final double size;
        private double life;

        private final RenderType LADYBUG = ladybugMain(LADYBUG_TEXTURE);
        private final RenderType OUTLINE = ladybugOutline(LADYBUG_OUTLINE_TEXTURE);

        private MagicLadybug(Vec3 pos, double size, double life) {
            this.localPos = pos;
            this.prevLocalPos = localPos;
            this.worldPos = pos;
            this.size = size;
            this.life = life;
        }

        private void move(Vec3 vec) {
            this.prevLocalPos = this.localPos;
            this.localPos = this.localPos.add(vec);
        }

        private void updateWorldPos(Quaternionf rotation, float partialTick) {
            Vec3 lerped = prevLocalPos.lerp(localPos, partialTick);
            Vector3f local = new Vector3f((float) lerped.x, (float) lerped.y, (float) lerped.z);
            local.rotate(rotation);
            this.worldPos = new Vec3(local.x, local.y, local.z);
        }

        private void renderOutline(MultiBufferSource multiBufferSource, PoseStack poseStack) {
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(-camera.getYRot()), (float) worldPos.x, (float) worldPos.y, (float) worldPos.z);
            poseStack.rotateAround(Axis.XP.rotationDegrees(camera.getXRot()), (float) worldPos.x, (float) worldPos.y, (float) worldPos.z);

            VertexConsumer ladybug_outline = multiBufferSource.getBuffer(OUTLINE);

            double quadSize = size * 0.47 / 0.5;
            ladybug_outline.addVertex(poseStack.last(), worldPos.add(-quadSize, quadSize, 0).toVector3f())
                    .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(-2);
            ladybug_outline.addVertex(poseStack.last(), worldPos.add(quadSize, quadSize, 0).toVector3f())
                    .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(-2);
            ladybug_outline.addVertex(poseStack.last(), worldPos.add(quadSize, -quadSize, 0).toVector3f())
                    .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(-2);
            ladybug_outline.addVertex(poseStack.last(), worldPos.add(-quadSize, -quadSize, 0).toVector3f())
                    .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(-2);
            poseStack.popPose();
        }

        private void render(MultiBufferSource multiBufferSource, PoseStack poseStack) {
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(-camera.getYRot()), (float) worldPos.x, (float) worldPos.y, (float) worldPos.z);
            poseStack.rotateAround(Axis.XP.rotationDegrees(camera.getXRot()), (float) worldPos.x, (float) worldPos.y, (float) worldPos.z);

            VertexConsumer ladybug = multiBufferSource.getBuffer(LADYBUG);

            double quadSize = size;
            float tint = 1f;
            ladybug.addVertex(poseStack.last(), worldPos.add(-quadSize, quadSize, 0).toVector3f())
                    .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), worldPos.add(quadSize, quadSize, 0).toVector3f())
                    .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), worldPos.add(quadSize, -quadSize, 0).toVector3f())
                    .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            ladybug.addVertex(poseStack.last(), worldPos.add(-quadSize, -quadSize, 0).toVector3f())
                    .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT).setColor(tint, tint, tint, 1.0f);
            poseStack.popPose();

            if (life > 0) life--;
        }

        private static void render(ArrayList<MagicLadybug> list, MultiBufferSource multiBufferSource, PoseStack poseStack, Quaternionf rotation, float partialTick) {
            for (MagicLadybug iterator : list) {
                iterator.updateWorldPos(rotation, partialTick);
                iterator.renderOutline(multiBufferSource, poseStack);
            }

            for (MagicLadybug bug : list) {
                bug.render(multiBufferSource, poseStack);
            }
            list.removeIf(bug -> bug.life <= 0);
        }
    }

    private double cometFunction(double x) {
        if (x > -0.03 && x < 0.439202) {
            return Math.sqrt(-Math.pow(x - 1.6 / 2, 2) + 0.7);
        }
        return (x + 1.6 / 2) * Math.pow(Math.E, -1 / 2.5 * (x + 1.6 / 2));
    }
}
