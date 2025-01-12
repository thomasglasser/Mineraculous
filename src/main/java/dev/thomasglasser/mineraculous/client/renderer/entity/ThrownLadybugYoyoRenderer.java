package dev.thomasglasser.mineraculous.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ThrownLadybugYoyoRenderer extends GeoEntityRenderer<ThrownLadybugYoyo> {
    public ThrownLadybugYoyoRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedItemGeoModel<>(Mineraculous.modLoc("ladybug_yoyo")));
    }

    @Override
    public void render(ThrownLadybugYoyo projectileEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (projectileEntity.getOwner() instanceof Player) {
            poseStack.pushPose();
            Player projectilePlayer = projectileEntity.getPlayerOwner();

            Vec3 vec3 = getPlayerHandPos(projectilePlayer, partialTick, MineraculousItems.LADYBUG_YOYO.get(), Minecraft.getInstance().getEntityRenderDispatcher());
            Vec3 projectilePos = new Vec3(projectileEntity.getX(), projectileEntity.getY(), projectileEntity.getZ());

            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(Mineraculous.modLoc("textures/yoyorope.png")));
            PoseStack.Pose pose = poseStack.last();

            Vec3 fromProjectileToHand = new Vec3(vec3.x - projectileEntity.getX(), vec3.y - projectileEntity.getY(), vec3.z - projectileEntity.getZ()); //relative to projectile
            Vec3 fromPTHOnXZ = new Vec3(fromProjectileToHand.x, 0, fromProjectileToHand.z); //fromProjectileToHandOnXZ
            Vec3 ropeThickness = new Vec3(getSegmentThickness(projectilePos, vec3).toVector3f());

            double maxLength = projectileEntity.maxRopeLength;

            if (fromProjectileToHand.length() >= projectileEntity.maxRopeLength || projectileEntity.maxRopeLength > 50) { //if it's bigger than 50 it starts looking wierd because IDK what hyperbolic cosine and catenary equation is, and for my sanity I don't want to know.
                vertex(vertexConsumer, pose, (float) -ropeThickness.x, (float) -ropeThickness.y, (float) -ropeThickness.z, 0f, 1f);
                vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x - ropeThickness.x), (float) (fromProjectileToHand.y - ropeThickness.y), (float) (fromProjectileToHand.z - ropeThickness.z), 1f, 1f);
                vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x + ropeThickness.x), (float) (fromProjectileToHand.y + ropeThickness.y), (float) (fromProjectileToHand.z + ropeThickness.z), 1f, 0f);
                vertex(vertexConsumer, pose, (float) +ropeThickness.x, (float) +ropeThickness.y, (float) +ropeThickness.z, 0f, 0f);
            } else {
                ArrayList<RopePoint> pointList = new ArrayList<>();
                int i = 0;
                int points = 100; //TODO maybe make this configurable
                final double T = 2048;
                for (i = 1; i <= points; i++) {
                    double xi = i * (fromPTHOnXZ.length()) / (points + 1);
                    double yi = i * (vec3.y - projectileEntity.getY()) / points - (2 * i * (maxLength - i * maxLength / (points + 1)) / (1 * T));
                    pointList.add(new RopePoint(xi, yi, i, fromPTHOnXZ));
                }

                Vec3 p0Pos = new Vec3(pointList.get(0).getX() + projectileEntity.getX(), pointList.get(0).getY() + projectileEntity.getY(), pointList.get(0).getZ() + projectileEntity.getZ());
                Vec3 projectileToP0Thickness = new Vec3(getSegmentThickness(projectilePos, p0Pos).toVector3f());
                vertex(vertexConsumer, pose, (float) (0 - projectileToP0Thickness.x), (float) (0 - projectileToP0Thickness.y), (float) (0 - projectileToP0Thickness.z), 0f, 1f);
                vertex(vertexConsumer, pose, (float) (pointList.get(0).getX() - projectileToP0Thickness.x), (float) (pointList.get(0).getY() - projectileToP0Thickness.y), (float) (pointList.get(0).getZ() - projectileToP0Thickness.z), 1f, 1f);
                vertex(vertexConsumer, pose, (float) (pointList.get(0).getX() + projectileToP0Thickness.x), (float) (pointList.get(0).getY() + projectileToP0Thickness.y), (float) (pointList.get(0).getZ() + projectileToP0Thickness.z), 1f, 0f);
                vertex(vertexConsumer, pose, (float) (0 + projectileToP0Thickness.x), (float) (0 + projectileToP0Thickness.y), (float) (0 + projectileToP0Thickness.z), 0f, 0f);

                for (i = 0; i <= points - 2; i++) {
                    Vec3 p1Pos = new Vec3(pointList.get(i).getX() + projectileEntity.getX(), pointList.get(i).getY() + projectileEntity.getY(), pointList.get(i).getZ() + projectileEntity.getZ());
                    Vec3 p2Pos = new Vec3(pointList.get(i + 1).getX() + projectileEntity.getX(), pointList.get(i + 1).getY() + projectileEntity.getY(), pointList.get(i + 1).getZ() + projectileEntity.getZ());
                    Vec3 point1ToPoint2Thickness = new Vec3(getSegmentThickness(p1Pos, p2Pos).toVector3f());
                    vertex(vertexConsumer, pose, (float) (pointList.get(i).getX() - point1ToPoint2Thickness.x), (float) (pointList.get(i).getY() - point1ToPoint2Thickness.y), (float) (pointList.get(i).getZ() - point1ToPoint2Thickness.z), 0f, 1f);
                    vertex(vertexConsumer, pose, (float) (pointList.get(i + 1).getX() - point1ToPoint2Thickness.x), (float) (pointList.get(i + 1).getY() - point1ToPoint2Thickness.y), (float) (pointList.get(i + 1).getZ() - point1ToPoint2Thickness.z), 1f, 1f);
                    vertex(vertexConsumer, pose, (float) (pointList.get(i + 1).getX() + point1ToPoint2Thickness.x), (float) (pointList.get(i + 1).getY() + point1ToPoint2Thickness.y), (float) (pointList.get(i + 1).getZ() + point1ToPoint2Thickness.z), 1f, 0f);
                    vertex(vertexConsumer, pose, (float) (pointList.get(i).getX() + point1ToPoint2Thickness.x), (float) (pointList.get(i).getY() + point1ToPoint2Thickness.y), (float) (pointList.get(i).getZ() + point1ToPoint2Thickness.z), 0f, 0f);
                }

                Vec3 lastPointPos = new Vec3(pointList.get(points - 1).getX() + projectileEntity.getX(), pointList.get(points - 1).getY() + projectileEntity.getY(), pointList.get(points - 1).getZ() + projectileEntity.getZ());
                Vec3 lastPointToHandThickness = new Vec3(getSegmentThickness(lastPointPos, vec3).toVector3f());
                vertex(vertexConsumer, pose, (float) (pointList.get(points - 1).getX() - lastPointToHandThickness.x), (float) (pointList.get(points - 1).getY() - lastPointToHandThickness.y), (float) (pointList.get(points - 1).getZ() - lastPointToHandThickness.z), 0f, 1f);
                vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x - lastPointToHandThickness.x), (float) (fromProjectileToHand.y - lastPointToHandThickness.y), (float) (fromProjectileToHand.z - lastPointToHandThickness.z), 1f, 1f);
                vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x + lastPointToHandThickness.x), (float) (fromProjectileToHand.y + lastPointToHandThickness.y), (float) (fromProjectileToHand.z + lastPointToHandThickness.z), 1f, 0f);
                vertex(vertexConsumer, pose, (float) (pointList.get(points - 1).getX() + lastPointToHandThickness.x), (float) (pointList.get(points - 1).getY() + lastPointToHandThickness.y), (float) (pointList.get(points - 1).getZ() + lastPointToHandThickness.z), 0f, 0f);
            }

            poseStack.popPose();
            super.render(projectileEntity, entityYaw, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    public static Vec3 getPlayerHandPos(Player player, float partialTick, Item item, EntityRenderDispatcher entityRenderDispatcher) {
        int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.is(item)) {
            i = -i;
        }

        float g = player.getAttackAnim(partialTick);
        float h = Mth.sin(Mth.sqrt(g) * 3.1415927F);
        float j = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * 0.017453292F;
        double d = Mth.sin(j);
        double e = Mth.cos(j);
        double k = (double) i * 0.35;
        if (entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player && entityRenderDispatcher.camera != null) { //ik the ide says its always true but i got a crash saying its null
            double n = 960.0 / (double) entityRenderDispatcher.options.fov().get();
            Vec3 vec3 = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) i * 0.9F, -1F);
            vec3 = vec3.scale(n);
            vec3 = vec3.yRot(h * 0.5F);
            vec3 = vec3.xRot(-h * 0.7F);
            return new Vec3(Mth.lerp(partialTick, player.xo, player.getX()) + vec3.x, Mth.lerp(partialTick, player.yo, player.getY()) + vec3.y + (double) player.getEyeHeight(), Mth.lerp((double) partialTick, player.zo, player.getZ()) + vec3.z);
        } else {
            float m = player.isCrouching() ? -0.1875F : 0.0F;
            return new Vec3(Mth.lerp(partialTick, player.xo, player.getX()) - e * k - d * 0.8, player.yo + (double) player.getEyeHeight() + (player.getY() - player.yo) * (double) partialTick - 0.45 + (double) m, Mth.lerp((double) partialTick, player.zo, player.getZ()) - d * k + e * 0.8);
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, float y, float z, float i, float j) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(-1).setUv(i, j).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownLadybugYoyo animatable) {
        return LadybugYoyoRenderer.TEXTURE;
    }

    private Vec3 getSegmentThickness(Vec3 p1, Vec3 p2) { //p1 projectile; p2 hand
        Vec3 fromP1P2 = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z); //relative to p1
        Vec3 fromP1P2OnXZ = new Vec3(fromP1P2.x, 0, fromP1P2.z);
        Vec3 watcherEyePos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        Vec3 fromP1ToPOV = new Vec3(watcherEyePos.x - p1.x, watcherEyePos.y - p1.y, watcherEyePos.z - p1.z); //fromPoint1ToPointOfView

        double XZGraphLineConst = fromP1P2OnXZ.z / fromP1P2OnXZ.x; // tan(alpha), alpha is the smallest angle between the fromP1P2OnXZ's support line and OX
        double XZGraphPerpendicularConst = -1 * fromP1P2OnXZ.x / fromP1P2OnXZ.z; // - ctan(alpha), the perpendicular from POV to fromP1P2OnXZ's support line.
        double offsetParallelLine = fromP1ToPOV.z - fromP1ToPOV.x * XZGraphPerpendicularConst;
        double projectionX = offsetParallelLine / (XZGraphLineConst * XZGraphLineConst + 1);
        double projectionZ = projectionX * XZGraphLineConst;

        Vec3 projXZ = new Vec3(projectionX, 0, projectionZ);
        double projXZlength = projXZ.length();//this SHOULD be negative, we need it to determine the projY
        if (!((fromP1P2OnXZ.x >= 0) == (projXZ.x >= 0))) {
            projXZlength = projXZlength * (-1); // here we are making it negative if needed 👆
        }

        double projectionY = fromP1P2.y * projXZlength / fromP1P2OnXZ.length();

        Vec3 fromPOVToRope = new Vec3(projectionX - fromP1ToPOV.x, projectionY - fromP1ToPOV.y, projectionZ - fromP1ToPOV.z);

        Vec3 segmentThinkness = fromPOVToRope.cross(fromP1ToPOV);
        segmentThinkness = segmentThinkness.scale(1 / segmentThinkness.length());
        segmentThinkness = segmentThinkness.scale(0.02d); //0.02 is the thinkness, TODO make it configurable maybe.
        return segmentThinkness;
    }
}
