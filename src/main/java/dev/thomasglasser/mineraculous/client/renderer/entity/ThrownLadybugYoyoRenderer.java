package dev.thomasglasser.mineraculous.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
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
        // TODO: Implement glowing middle part
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(ThrownLadybugYoyo projectileEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (projectileEntity.getOwner() instanceof Player player) {
            poseStack.pushPose();
            Player projectilePlayer = projectileEntity.getPlayerOwner();

            Vec3 vec3 = getPlayerHandPos(projectilePlayer, partialTick, MineraculousItems.LADYBUG_YOYO.get(), Minecraft.getInstance().getEntityRenderDispatcher());

            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(Mineraculous.modLoc("textures/yoyorope.png")));
            PoseStack.Pose pose = poseStack.last();

            Vec3 fromProjectileToHand = new Vec3(vec3.x - projectileEntity.getX(), vec3.y - projectileEntity.getY(), vec3.z - projectileEntity.getZ()); //relative to projectile
            Vec3 fromPTHOnXZ = new Vec3(fromProjectileToHand.x, 0, fromProjectileToHand.z); //fromProjectileToHandOnXZ
            Vec3 watcherEyePos = Minecraft.getInstance().cameraEntity.getEyePosition();
            Vec3 fromProjectileToPOV = new Vec3(watcherEyePos.x - projectileEntity.getX(), watcherEyePos.y - projectileEntity.getY(), watcherEyePos.z - projectileEntity.getZ()); //fromProjectileToPointOfView

            double XZGraphLineConst = fromPTHOnXZ.z / fromPTHOnXZ.x; // tan(alpha), alpha is the smallest angle between the fromPTHOnXZ's support line and OX
            double XZGraphPerpendicularConst = -1 * fromPTHOnXZ.x / fromPTHOnXZ.z; // - ctan(alpha), the perpendicular from POV to fromPTHOnXZ's support line.
            double offsetParallelLine = fromProjectileToPOV.z - fromProjectileToPOV.x * XZGraphPerpendicularConst;
            double projectionX = offsetParallelLine / (XZGraphLineConst * XZGraphLineConst + 1);
            double projectionZ = projectionX * XZGraphLineConst;

            Vec3 projXZ = new Vec3(projectionX, 0, projectionZ);
            double projXZlength = projXZ.length();//this can be negative, we need it to determine the projY
            if (!((fromPTHOnXZ.x >= 0) == (projXZ.x >= 0))) {
                projXZlength = projXZlength * (-1);
            }

            double projectionY = fromProjectileToHand.y * projXZlength / fromPTHOnXZ.length();

            Vec3 fromPOVToRope = new Vec3(projectionX - fromProjectileToPOV.x, projectionY - fromProjectileToPOV.y, projectionZ - fromProjectileToPOV.z);

            Vec3 ropeThinkness = fromPOVToRope.cross(fromProjectileToPOV);
            ropeThinkness = ropeThinkness.scale(1 / ropeThinkness.length());
            ropeThinkness = ropeThinkness.scale(0.03d); //0.03 is the thinkness, TODO make it configurable maybe.

            vertex(vertexConsumer, pose, (float) -ropeThinkness.x, (float) -ropeThinkness.y, (float) -ropeThinkness.z, 0f, 1f);
            vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x - ropeThinkness.x), (float) (fromProjectileToHand.y - ropeThinkness.y), (float) (fromProjectileToHand.z - ropeThinkness.z), 1f, 1f);
            vertex(vertexConsumer, pose, (float) (fromProjectileToHand.x + ropeThinkness.x), (float) (fromProjectileToHand.y + ropeThinkness.y), (float) (fromProjectileToHand.z + ropeThinkness.z), 1f, 0f);
            vertex(vertexConsumer, pose, (float) +ropeThinkness.x, (float) +ropeThinkness.y, (float) +ropeThinkness.z, 0f, 0f);

            poseStack.popPose();
            super.render(projectileEntity, entityYaw, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    private static Vec3 getPlayerHandPos(Player player, float partialTick, Item item, EntityRenderDispatcher entityRenderDispatcher) {
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
        if (entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double n = 960.0 / (double) entityRenderDispatcher.options.fov().get();
            Vec3 vec3 = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) i * 0.525F, -0.1F);
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
}
