package dev.thomasglasser.mineraculous.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.level.storage.PerchCatStaffData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CatStaffRenderer extends GeoItemRenderer<CatStaffItem> {
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/item/geo/cat_staff.png");

    public CatStaffRenderer() {
        super(new DefaultedItemGeoModel<>(Mineraculous.modLoc("cat_staff")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CatStaffItem animatable) {
        return TEXTURE;
    }

    public static void renderCatStaffPerch(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTicks) {
        float PIXEL = 1 / 16f;
        PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
        float length = perchData.length();
        boolean catStaffPerchRender = perchData.canRender();
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(Mineraculous.modLoc("textures/misc/cat_staff_perching.png")));
        PoseStack.Pose pose = poseStack.last();

        //STAFF - PLAYER NEW
        float bodyAngle = perchData.initPos().y;
        Vector3f bodyDirectionF = perchData.initPos();
        double d0, d1, d2;
        d0 = Mth.lerp(partialTicks, player.xo, player.getX());
        d1 = Mth.lerp(partialTicks, player.zo, player.getZ());
        d2 = Mth.lerp(partialTicks, player.yo, player.getY());
        float bodyY = perchData.isFalling() ? (float) (perchData.yBeforeFalling() - d2) : 0;
        bodyDirectionF = new Vector3f((float) (bodyDirectionF.x - d0), bodyY, (float) (bodyDirectionF.z - d1));
        int direction = (int) ((bodyAngle + 45f) % 360) / 90;
        if (catStaffPerchRender) {
            float top = player.getBbHeight() + 0.2f + bodyDirectionF.y;
            float nLength = length + bodyDirectionF.y;
            if (perchData.isFalling()) {
                Vec3 vertical = new Vec3(0, 1, 0);
                Vec3 movement = new Vec3(perchData.initialFallDirection());
                Vec3 left = new Vec3(vertical.cross(movement).toVector3f());
                left = left.normalize();
                Vec3 staffOriginToPlayer = new Vec3(bodyDirectionF.x, nLength, bodyDirectionF.z);
                double cosTheta = new Vec3(bodyDirectionF.x, 0, bodyDirectionF.z).length() / staffOriginToPlayer.length();
                double theta = Math.acos(cosTheta);
                theta = Math.PI / 2 - theta;
                Quaternionf q = new Quaternionf(left.x * Math.sin(theta / 2), left.y * Math.sin(theta / 2), left.z * Math.sin(theta / 2), Math.cos(theta / 2));
                poseStack.rotateAround(q, bodyDirectionF.x, bodyY + length, bodyDirectionF.z);

            }
            //SIDES:
            drawAllStaffSides(vertexConsumer, pose, light, bodyDirectionF.x, bodyDirectionF.z, top, nLength, 1);

            //UP&DOWN:
            drawCap(vertexConsumer, pose, light, top, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
            drawCap(vertexConsumer, pose, light, light, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
            //PAW:
            float eyeY = player.getEyeHeight(Pose.STANDING) + bodyDirectionF.y;
            float pawBottom = eyeY - 0.1f;
            float[][] offsets = {
                    { -1 / 20f, 1 / 20f, -(PIXEL + 0.001f), -(PIXEL + 0.001f) }, // 0
                    { PIXEL + 0.001f, PIXEL + 0.001f, -1 / 20f, 1 / 20f },       // 1
                    { -1 / 20f, 1 / 20f, PIXEL + 0.001f, PIXEL + 0.001f },       // 2
                    { -(PIXEL + 0.001f), -(PIXEL + 0.001f), -1 / 20f, 1 / 20f }  // 3
            };
            float[] o = offsets[direction];
            float x1 = o[0] + bodyDirectionF.x();
            float x2 = o[1] + bodyDirectionF.x();
            float z1 = o[2] + bodyDirectionF.z();
            float z2 = o[3] + bodyDirectionF.z();
            light = 15728880;
            float uMin = 4 / 16f, uMax = 14 / 16f;
            float vMin = 0f, vMax = 10 / 16f;
            vertex(vertexConsumer, pose, x1, eyeY, z1, uMin, vMin, light);
            vertex(vertexConsumer, pose, x1, pawBottom, z1, uMin, vMax, light);
            vertex(vertexConsumer, pose, x2, pawBottom, z2, uMax, vMax, light);
            vertex(vertexConsumer, pose, x2, eyeY, z2, uMax, vMin, light);
            //LINES:
            float y1 = eyeY - PIXEL * 3f;
            float y2 = eyeY - PIXEL * 3.5f;
            float offset = 0.0001f;
            float v = PIXEL * 11.5f;
            float u1 = PIXEL * 12;
            float u2 = 1;
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x(), y1, -PIXEL + bodyDirectionF.z() - offset,
                    -PIXEL + bodyDirectionF.x(), y2, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), y2, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), y1, -PIXEL + bodyDirectionF.z() - offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x(), y1, +PIXEL + bodyDirectionF.z() + offset,
                    +PIXEL + bodyDirectionF.x(), y2, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), y2, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), y1, +PIXEL + bodyDirectionF.z() + offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x() + offset, y1, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y2, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y2, -PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y1, -PIXEL + bodyDirectionF.z());
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x() - offset, y1, -PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y2, -PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y2, +PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y1, +PIXEL + bodyDirectionF.z());
            y1 = PIXEL + eyeY;
            y2 = PIXEL * 1.5f + eyeY;
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x(), y1, -PIXEL + bodyDirectionF.z() - offset,
                    -PIXEL + bodyDirectionF.x(), y2, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), y2, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), y1, -PIXEL + bodyDirectionF.z() - offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x(), y1, +PIXEL + bodyDirectionF.z() + offset,
                    +PIXEL + bodyDirectionF.x(), y2, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), y2, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), y1, +PIXEL + bodyDirectionF.z() + offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x() + offset, y1, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y2, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y2, -PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, y1, -PIXEL + bodyDirectionF.z());
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x() - offset, y1, -PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y2, -PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y2, +PIXEL + bodyDirectionF.z(),
                    -PIXEL + bodyDirectionF.x() - offset, y1, +PIXEL + bodyDirectionF.z());
            float P1 = PIXEL * 4.5f;
            float P2 = PIXEL * 5f;
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x(), P1 + eyeY, -PIXEL + bodyDirectionF.z() - offset,
                    -PIXEL + bodyDirectionF.x(), P2 + eyeY, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), P2 + eyeY, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), P1 + eyeY, -PIXEL + bodyDirectionF.z() - offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x(), P1 + eyeY, +PIXEL + bodyDirectionF.z() + offset,
                    +PIXEL + bodyDirectionF.x(), P2 + eyeY, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), P2 + eyeY, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), P1 + eyeY, +PIXEL + bodyDirectionF.z() + offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x() + offset, P1 + eyeY, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, P2 + eyeY, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, P2 + eyeY, -PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, P1 + eyeY, -PIXEL + bodyDirectionF.z());
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL - offset + bodyDirectionF.x(), P1 + eyeY, -PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), P2 + eyeY, -PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), P2 + eyeY, +PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), P1 + eyeY, +PIXEL + bodyDirectionF.z());
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + nLength, -PIXEL + bodyDirectionF.z() - offset,
                    -PIXEL + bodyDirectionF.x(), PIXEL + nLength, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), PIXEL + nLength, -PIXEL + bodyDirectionF.z() - offset,
                    +PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + nLength, -PIXEL + bodyDirectionF.z() - offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + nLength, +PIXEL + bodyDirectionF.z() + offset,
                    +PIXEL + bodyDirectionF.x(), PIXEL + nLength, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), PIXEL + nLength, +PIXEL + bodyDirectionF.z() + offset,
                    -PIXEL + bodyDirectionF.x(), PIXEL * 0.5f + nLength, +PIXEL + bodyDirectionF.z() + offset);
            quad(vertexConsumer, pose, light, u1, u2, v,
                    +PIXEL + bodyDirectionF.x() + offset, PIXEL * 0.5f + nLength, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, PIXEL + nLength, +PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, PIXEL + nLength, -PIXEL + bodyDirectionF.z(),
                    +PIXEL + bodyDirectionF.x() + offset, PIXEL * 0.5f + nLength, -PIXEL + bodyDirectionF.z());
            quad(vertexConsumer, pose, light, u1, u2, v,
                    -PIXEL - offset + bodyDirectionF.x(), PIXEL * 0.5f + nLength, -PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), PIXEL + nLength, -PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), PIXEL + nLength, +PIXEL + bodyDirectionF.z(),
                    -PIXEL - offset + bodyDirectionF.x(), PIXEL * 0.5f + nLength, +PIXEL + bodyDirectionF.z());
        }
        poseStack.popPose();
    }

    private static void drawAllStaffSides(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float xOffset, float zOffset, float topY, float bottomY, float uvOffset) {
        float PIXEL = 1 / 16f;
        drawStaffSide(vertexConsumer, pose, light, uvOffset,
                -PIXEL + xOffset, topY, +PIXEL + zOffset,
                -PIXEL + xOffset, bottomY, +PIXEL + zOffset,
                -PIXEL + xOffset, bottomY, -PIXEL + zOffset,
                -PIXEL + xOffset, topY, -PIXEL + zOffset);

        drawStaffSide(vertexConsumer, pose, light, uvOffset,
                -PIXEL + xOffset, topY, -PIXEL + zOffset,
                -PIXEL + xOffset, bottomY, -PIXEL + zOffset,
                +PIXEL + xOffset, bottomY, -PIXEL + zOffset,
                +PIXEL + xOffset, topY, -PIXEL + zOffset);

        drawStaffSide(vertexConsumer, pose, light, uvOffset,
                +PIXEL + xOffset, topY, -PIXEL + zOffset,
                +PIXEL + xOffset, bottomY, -PIXEL + zOffset,
                +PIXEL + xOffset, bottomY, +PIXEL + zOffset,
                +PIXEL + xOffset, topY, +PIXEL + zOffset);

        drawStaffSide(vertexConsumer, pose, light, uvOffset,
                +PIXEL + xOffset, topY, +PIXEL + zOffset,
                +PIXEL + xOffset, bottomY, +PIXEL + zOffset,
                -PIXEL + xOffset, bottomY, +PIXEL + zOffset,
                -PIXEL + xOffset, topY, +PIXEL + zOffset);
    }

    private static void drawStaffSide(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float uvOffset, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        float PIXEL = 1 / 16f;
        vertex(vertexConsumer, pose, x1, y1, z1, 0f, 0f, light);
        vertex(vertexConsumer, pose, x2, y2, z2, 0f, uvOffset, light);
        vertex(vertexConsumer, pose, x3, y3, z3, PIXEL * 4, uvOffset, light);
        vertex(vertexConsumer, pose, x4, y4, z4, PIXEL * 4, 0f, light);
    }

    private static void drawCap(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light,
            float y, float xOffset, float zOffset,
            float u1, float v1, float u2, float v2) {
        float PIXEL = 1 / 16f;
        vertex(vertexConsumer, pose, -PIXEL + xOffset, y, -PIXEL + zOffset, u1, v1, light);
        vertex(vertexConsumer, pose, +PIXEL + xOffset, y, -PIXEL + zOffset, u1, v2, light);
        vertex(vertexConsumer, pose, +PIXEL + xOffset, y, +PIXEL + zOffset, u2, v2, light);
        vertex(vertexConsumer, pose, -PIXEL + xOffset, y, +PIXEL + zOffset, u2, v1, light);
    }

    private static void quad(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float u1, float u2, float v,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4) {
        vertex(vertexConsumer, pose, x1, y1, z1, u1, v, light);
        vertex(vertexConsumer, pose, x2, y2, z2, u1, v, light);
        vertex(vertexConsumer, pose, x3, y3, z3, u2, v, light);
        vertex(vertexConsumer, pose, x4, y4, z4, u2, v, light);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float i, float j, int light) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(-1).setUv(i, j).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
    }
}
