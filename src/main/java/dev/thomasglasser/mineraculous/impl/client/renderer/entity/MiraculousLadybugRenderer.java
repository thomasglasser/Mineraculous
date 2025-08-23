package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import static net.minecraft.client.renderer.RenderStateShard.CULL;
import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.OVERLAY;
import static net.minecraft.client.renderer.RenderStateShard.POLYGON_OFFSET_LAYERING;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_EYES_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
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

    public MiraculousLadybugRenderer(EntityRendererProvider.Context context) {
        super(context);
        for (int i = 1; i <= 10; i++) {
            double r = 0.1 + (Math.random() * (0.3 - 0.1));
            magicLadybugs.add(new MagicLadybug(
                    new Vec3(Math.random() * 10.0 - 0.5, Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 1.0),
                    r, 20));
        }
    }

    private static final ResourceLocation LADYBUG_TEXTURE = Mineraculous.modLoc("textures/particle/ladybug.png");
    private static final ResourceLocation LADYBUG_OUTLINE_TEXTURE = Mineraculous.modLoc("textures/particle/ladybug_glow.png");

    @Override
    public void render(MiraculousLadybug entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        Vec3 lookVec = entity.getLookAngle().normalize().scale(-1); // direction to point the swarm

        // quaternion rotation from +X → lookVec
        Vec3 base = new Vec3(1, 0, 0);
        Vec3 axis = base.cross(lookVec).normalize();
        double angle = Math.acos(base.dot(lookVec));
        Quaternionf rotation = new Quaternionf();
        if (!axis.equals(Vec3.ZERO)) {
            rotation = new Quaternionf().fromAxisAngleRad(axis.toVector3f(), (float) angle);
        }

        if (magicLadybugs.size() < 200) {
            for (int i = 1; i <= 10; i++) {
                double r = 0.1 + (Math.random() * (0.3 - 0.1));
                MagicLadybug it = new MagicLadybug(
                        new Vec3(Math.random() * 10.0 - 0.5, Math.random(), Math.random()), r, 20);

                double min = Math.min(cometFunction(it.localPos.horizontalDistance()), -cometFunction(it.localPos.horizontalDistance()));
                double max = Math.max(cometFunction(it.localPos.horizontalDistance()), -cometFunction(it.localPos.horizontalDistance()));

                double randomY = min + (Math.random() * (max - min));
                double randomZ = min + (Math.random() * (max - min));
                it.localPos = new Vec3(it.localPos.x, randomY, randomZ);
                magicLadybugs.add(it);
            }
        }

        // update local movement (always along +X)
        for (MagicLadybug it : magicLadybugs) {
            it.move(new Vec3(0.1, 0, 0));

            if (it.localPos.y > cometFunction(it.localPos.x))
                it.localPos = new Vec3(it.localPos.x, cometFunction(it.localPos.x), it.localPos.z);
            if (it.localPos.y < -cometFunction(it.localPos.x))
                it.localPos = new Vec3(it.localPos.x, -cometFunction(it.localPos.x), it.localPos.z);
            if (it.localPos.z > cometFunction(it.localPos.x))
                it.localPos = new Vec3(it.localPos.x, it.localPos.y, cometFunction(it.localPos.x));
            if (it.localPos.z < -cometFunction(it.localPos.x))
                it.localPos = new Vec3(it.localPos.x, it.localPos.y, -cometFunction(it.localPos.x));
        }

        poseStack.translate(-0.5, 0, -0.5);
        MagicLadybug.render(magicLadybugs, bufferSource, poseStack, rotation);
        poseStack.translate(0.5, 0, 0.5);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MiraculousLadybug entity) {
        return Mineraculous.modLoc("textures/item/empty.png");
    }

    private class MagicLadybug {
        private Vec3 localPos;   // path-space position
        private Vec3 worldPos;   // rotated each frame
        private final double size;
        private double life;

        private final RenderType LADYBUG = ladybugMain(LADYBUG_TEXTURE);
        private final RenderType OUTLINE = ladybugOutline(LADYBUG_OUTLINE_TEXTURE);

        private MagicLadybug(Vec3 pos, double size, double life) {
            this.localPos = pos;
            this.worldPos = pos;
            this.size = size;
            this.life = life;
        }

        private void move(Vec3 vec) {
            this.localPos = this.localPos.add(vec);
        }

        private void updateWorldPos(Quaternionf rotation) {
            Vector3f local = new Vector3f((float) localPos.x, (float) localPos.y, (float) localPos.z);
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

        private static void render(ArrayList<MagicLadybug> list, MultiBufferSource multiBufferSource,
                PoseStack poseStack, Quaternionf rotation) {
            for (MagicLadybug iterator : list) {
                iterator.updateWorldPos(rotation);
                iterator.renderOutline(multiBufferSource, poseStack);
            }

            for (MagicLadybug bug : list) {
                bug.render(multiBufferSource, poseStack);
            }
            list.removeIf(bug -> bug.life <= 0);
        }
    }

    //TODO move this
    public static RenderType ladybugOutline(ResourceLocation texture) {
        return RenderType.create(
                "ladybug_outline",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_EYES_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, true, true))
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setLayeringState(POLYGON_OFFSET_LAYERING)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    }

    public static RenderType ladybugMain(ResourceLocation texture) {
        return RenderType.create(
                "ladybug_main",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_EYES_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, true))
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));
    }

    private double cometFunction(double x) {
        if (x > -0.03 && x < 0.439202) {
            return Math.sqrt(-Math.pow(x - 1.6 / 2, 2) + 0.7);
        }
        return (x + 1.6 / 2) * Math.pow(Math.E, -1 / 2.5 * (x + 1.6 / 2));
    }
}
