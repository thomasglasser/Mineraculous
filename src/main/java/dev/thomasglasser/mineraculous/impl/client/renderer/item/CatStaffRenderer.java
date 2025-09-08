package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.renderer.item.GlowingDefaultedGeoItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CatStaffRenderer extends GlowingDefaultedGeoItemRenderer<CatStaffItem> {
    public static final ResourceLocation EXTENDED_LOCATION = Mineraculous.modLoc("textures/misc/cat_staff.png");
    public static final ResourceLocation SPYGLASS_SCOPE_LOCATION = Mineraculous.modLoc("textures/misc/cat_staff_spyglass_scope.png");
    public static final ResourceLocation SPYGLASS_LOCATION = makeTextureLocation(Mineraculous.modLoc("cat_staff_spyglass"));
    public static final ResourceLocation PHONE_LOCATION = makeTextureLocation(Mineraculous.modLoc("cat_staff_phone"));

    private static final float PIXEL = 1 / 16f;

    public CatStaffRenderer() {
        super(MineraculousItems.CAT_STAFF.getId());
    }

    @Override
    public ResourceLocation getTextureLocation(CatStaffItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null) {
            CatStaffItem.Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
            if (ability == CatStaffItem.Ability.PHONE) {
                return PHONE_LOCATION;
            } else if (ability == CatStaffItem.Ability.SPYGLASS) {
                return SPYGLASS_LOCATION;
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Override
    public void defaultRender(PoseStack poseStack, CatStaffItem animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        ItemStack stack = getCurrentItemStack();
        if (stack.get(MineraculousDataComponents.CARRIER) != null) {
            Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
            if (carrierId != null && Minecraft.getInstance().level != null) {
                Entity carrier = Minecraft.getInstance().level.getEntity(carrierId);
                if (carrier != null) {
                    CatStaffItem.Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
                    if (ability == CatStaffItem.Ability.PERCH || ability == CatStaffItem.Ability.TRAVEL) {
                        TravelingCatStaffData travelingCatStaffData = carrier.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
                        PerchingCatStaffData perchingCatStaffData = carrier.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                        if (travelingCatStaffData.traveling() || perchingCatStaffData.perching()) {
                            boolean firstPersonHand = this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                            boolean thirdPersonHand = this.renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || this.renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

                            if (MineraculousClientUtils.isFirstPerson() &&
                                    MineraculousClientUtils.getCameraEntity() == carrier &&
                                    firstPersonHand) {
                                return;
                            }

                            if (thirdPersonHand) return;

                        }
                    }
                }
            }
        }
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    public static class PerchRenderer {
        private float oLength;
        private float smoothedLength;

        public void tick(Player player) {
            PerchingCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
            smoothedLength += (perchData.length() - smoothedLength) * 0.6f;
            oLength = smoothedLength;
        }

        public void renderPerch(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
            ItemStack offhandItem = player.getOffhandItem();
            ItemStack mainHandItem = player.getMainHandItem();

            boolean lHCatStaffPerch = offhandItem.is(MineraculousItems.CAT_STAFF) && offhandItem.has(MineraculousDataComponents.ACTIVE) && offhandItem.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;
            boolean rHCatStaffPerch = mainHandItem.is(MineraculousItems.CAT_STAFF) && mainHandItem.has(MineraculousDataComponents.ACTIVE) && mainHandItem.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.PERCH;

            if (lHCatStaffPerch || rHCatStaffPerch) {
                PerchingCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                float length = Mth.lerp(partialTick, oLength, perchData.length());
                boolean catStaffPerchRender = perchData.canRender();
                poseStack.pushPose();
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(EXTENDED_LOCATION));
                PoseStack.Pose pose = poseStack.last();

                //STAFF - PLAYER NEW
                float bodyAngle = perchData.initPos().y;
                Vector3f bodyDirectionF = perchData.initPos();
                double d0, d1, d2;
                d0 = Mth.lerp(partialTick, player.xo, player.getX());
                d1 = Mth.lerp(partialTick, player.zo, player.getZ());
                d2 = Mth.lerp(partialTick, player.yo, player.getY());
                float bodyY = perchData.isFalling() ? (float) (perchData.yBeforeFalling() - d2) : 0;
                bodyDirectionF = new Vector3f((float) (bodyDirectionF.x - d0), bodyY, (float) (bodyDirectionF.z - d1));
                int direction = (int) ((bodyAngle + 45f) % 360) / 90;
                if (catStaffPerchRender) {
                    float top = player.getEyeHeight(Pose.STANDING) + 0.4f + bodyDirectionF.y;
                    float nLength = length + bodyDirectionF.y;
                    if (perchData.isFalling()) {
                        Vector3f vertical = new Vector3f(0, 1, 0);
                        Vec3 staffOriginToPlayer = new Vec3(bodyDirectionF.x, nLength, bodyDirectionF.z);
                        Vector3f rot = staffOriginToPlayer.add(0, 1, 0).normalize().scale(-1).toVector3f();
                        Quaternionf q = new Quaternionf().rotateTo(vertical, rot);
                        poseStack.rotateAround(q, bodyDirectionF.x, bodyDirectionF.y + length, bodyDirectionF.z);

                    }
                    //SIDES:
                    drawAllStaffSides(vertexConsumer, pose, light, bodyDirectionF.x, bodyDirectionF.z, top, nLength, 1);
                    //UP&DOWN:
                    drawCap(vertexConsumer, pose, light, top, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
                    drawCap(vertexConsumer, pose, light, light, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
                    //PAW & LINES
                    drawPawAndLines(vertexConsumer, pose, bodyDirectionF, direction, nLength, player.getEyeHeight(Pose.STANDING) + bodyDirectionF.y);

                    //oLength = perchData.length();
                }
                poseStack.popPose();
            }
        }
    }

    public static void renderTravel(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick) {
        ItemStack offhandItem = player.getOffhandItem();
        ItemStack mainHandItem = player.getMainHandItem();

        boolean lHCatStaffTravel = offhandItem.is(MineraculousItems.CAT_STAFF) && offhandItem.has(MineraculousDataComponents.ACTIVE) && offhandItem.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.TRAVEL;
        boolean rHCatStaffTravel = mainHandItem.is(MineraculousItems.CAT_STAFF) && mainHandItem.has(MineraculousDataComponents.ACTIVE) && mainHandItem.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == CatStaffItem.Ability.TRAVEL;

        if (lHCatStaffTravel || rHCatStaffTravel) {
            TravelingCatStaffData travelingCatStaffData = player.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
            float length = travelingCatStaffData.length();
            BlockPos target = travelingCatStaffData.blockPos();
            poseStack.pushPose();
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(EXTENDED_LOCATION));
            PoseStack.Pose pose = poseStack.last();

            double d0, d1, d2;
            d0 = Mth.lerp(partialTick, player.xo, player.getX());
            d1 = Mth.lerp(partialTick, player.zo, player.getZ());
            d2 = Mth.lerp(partialTick, player.yo, player.getY());
            if (travelingCatStaffData.traveling()) {
                //ROTATE
                Vec3 staffOriginToPlayer = new Vec3(
                        target.getX() - d0,
                        target.getY() - d2,
                        target.getZ() - d1);
                Vector3f bodyDirectionF = staffOriginToPlayer.add(0, 1, 0).toVector3f();
                Vector3f vertical = new Vector3f(0, 1, 0);
                Vector3f rot = staffOriginToPlayer.add(0, 1, 0).normalize().scale(-1).toVector3f();
                Quaternionf q = new Quaternionf().rotateTo(vertical, rot);
                poseStack.rotateAround(q, bodyDirectionF.x, bodyDirectionF.y, bodyDirectionF.z);

                //SIDES
                boolean didLaunch = travelingCatStaffData.launch();
                float top = (float) new Vec3(0, target.getY() - d2 + staffOriginToPlayer.length(), 0).length();
                float bottom = bodyDirectionF.y;
                if (!didLaunch && length < staffOriginToPlayer.length()) {
                    bottom = (float) new Vec3(0, target.getY() - d2 + staffOriginToPlayer.length() - length, 0).length();
                    if (bottom > top) {
                        bottom = bodyDirectionF.y;
                    }
                }
                top += player.getBbHeight();
                drawAllStaffSides(vertexConsumer, pose, light, bodyDirectionF.x, bodyDirectionF.z, top + PIXEL * 2, bottom, 1);
                //UP&DOWN:
                drawCap(vertexConsumer, pose, light, top, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
                drawCap(vertexConsumer, pose, light, light, bodyDirectionF.x, bodyDirectionF.z, PIXEL * 12, PIXEL * 12, 1f, 1f);
                //PAW & LINES:
                drawPawAndLines(vertexConsumer, pose, bodyDirectionF, (int) ((travelingCatStaffData.initBodAngle() + 45f) % 360) / 90, top, top - 1);

            }
            poseStack.popPose();
        }
    }

    private static void drawPawAndLines(VertexConsumer vertexConsumer, PoseStack.Pose pose, Vector3f bodyDirectionF, int direction, float nLength, float eyeY) {
        //PAW:
        float pawTop = eyeY + 0.2f;
        float pawBottom = pawTop - 0.1f;
        float[][] offsets = {
                { -1 / 20f, 1 / 20f, -(PIXEL + 0.001f), -(PIXEL + 0.001f) }, // 0
                { PIXEL + 0.001f, PIXEL + 0.001f, -1 / 20f, 1 / 20f },       // 1
                { -1 / 20f, 1 / 20f, PIXEL + 0.001f, PIXEL + 0.001f },       // 2
                { -(PIXEL + 0.001f), -(PIXEL + 0.001f), -1 / 20f, 1 / 20f }  // 3
        };
        direction = direction >= 4 ? direction % 4 : direction;
        direction = direction < 0 ? -direction : direction;
        float[] o = offsets[direction];
        float x1 = o[0] + bodyDirectionF.x();
        float x2 = o[1] + bodyDirectionF.x();
        float z1 = o[2] + bodyDirectionF.z();
        float z2 = o[3] + bodyDirectionF.z();
        int light = 15728880;
        float uMin = 4 / 16f, uMax = 14 / 16f;
        float vMin = 0f, vMax = 10 / 16f;
        vertex(vertexConsumer, pose, x1, pawTop, z1, uMin, vMin, light);
        vertex(vertexConsumer, pose, x1, pawBottom, z1, uMin, vMax, light);
        vertex(vertexConsumer, pose, x2, pawBottom, z2, uMax, vMax, light);
        vertex(vertexConsumer, pose, x2, pawTop, z2, uMax, vMin, light);
        //LINES:
        float y1 = eyeY - PIXEL * 4f;
        float y2 = eyeY - PIXEL * 4.5f;
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
        y1 = PIXEL + eyeY - 0.1f;
        y2 = PIXEL * 1.5f + eyeY - 0.1f; //middle
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
        float P1 = PIXEL * 4.5f + eyeY;
        float P2 = PIXEL * 5f + eyeY;
        quad(vertexConsumer, pose, light, u1, u2, v,
                -PIXEL + bodyDirectionF.x(), P1, -PIXEL + bodyDirectionF.z() - offset,
                -PIXEL + bodyDirectionF.x(), P2, -PIXEL + bodyDirectionF.z() - offset,
                +PIXEL + bodyDirectionF.x(), P2, -PIXEL + bodyDirectionF.z() - offset,
                +PIXEL + bodyDirectionF.x(), P1, -PIXEL + bodyDirectionF.z() - offset);
        quad(vertexConsumer, pose, light, u1, u2, v,
                +PIXEL + bodyDirectionF.x(), P1, +PIXEL + bodyDirectionF.z() + offset,
                +PIXEL + bodyDirectionF.x(), P2, +PIXEL + bodyDirectionF.z() + offset,
                -PIXEL + bodyDirectionF.x(), P2, +PIXEL + bodyDirectionF.z() + offset,
                -PIXEL + bodyDirectionF.x(), P1, +PIXEL + bodyDirectionF.z() + offset);
        quad(vertexConsumer, pose, light, u1, u2, v,
                +PIXEL + bodyDirectionF.x() + offset, P1, +PIXEL + bodyDirectionF.z(),
                +PIXEL + bodyDirectionF.x() + offset, P2, +PIXEL + bodyDirectionF.z(),
                +PIXEL + bodyDirectionF.x() + offset, P2, -PIXEL + bodyDirectionF.z(),
                +PIXEL + bodyDirectionF.x() + offset, P1, -PIXEL + bodyDirectionF.z());
        quad(vertexConsumer, pose, light, u1, u2, v,
                -PIXEL - offset + bodyDirectionF.x(), P1, -PIXEL + bodyDirectionF.z(),
                -PIXEL - offset + bodyDirectionF.x(), P2, -PIXEL + bodyDirectionF.z(),
                -PIXEL - offset + bodyDirectionF.x(), P2, +PIXEL + bodyDirectionF.z(),
                -PIXEL - offset + bodyDirectionF.x(), P1, +PIXEL + bodyDirectionF.z());
    }

    private static void drawAllStaffSides(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float xOffset, float zOffset, float topY, float bottomY, float uvOffset) {
        float localTop = topY;
        while (localTop > bottomY) {
            float localBottom = Math.max(bottomY, localTop - 1);
            drawStaffSide(vertexConsumer, pose, light, uvOffset,
                    -PIXEL + xOffset, localTop, +PIXEL + zOffset,
                    -PIXEL + xOffset, localBottom, +PIXEL + zOffset,
                    -PIXEL + xOffset, localBottom, -PIXEL + zOffset,
                    -PIXEL + xOffset, localTop, -PIXEL + zOffset);

            drawStaffSide(vertexConsumer, pose, light, uvOffset,
                    -PIXEL + xOffset, localTop, -PIXEL + zOffset,
                    -PIXEL + xOffset, localBottom, -PIXEL + zOffset,
                    +PIXEL + xOffset, localBottom, -PIXEL + zOffset,
                    +PIXEL + xOffset, localTop, -PIXEL + zOffset);

            drawStaffSide(vertexConsumer, pose, light, uvOffset,
                    +PIXEL + xOffset, localTop, -PIXEL + zOffset,
                    +PIXEL + xOffset, localBottom, -PIXEL + zOffset,
                    +PIXEL + xOffset, localBottom, +PIXEL + zOffset,
                    +PIXEL + xOffset, localTop, +PIXEL + zOffset);

            drawStaffSide(vertexConsumer, pose, light, uvOffset,
                    +PIXEL + xOffset, localTop, +PIXEL + zOffset,
                    +PIXEL + xOffset, localBottom, +PIXEL + zOffset,
                    -PIXEL + xOffset, localBottom, +PIXEL + zOffset,
                    -PIXEL + xOffset, localTop, +PIXEL + zOffset);
            localTop -= 1.000f;
        }
    }

    private static void drawStaffSide(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light, float uvOffset, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(vertexConsumer, pose, x1, y1, z1, 0f, 0f, light);
        vertex(vertexConsumer, pose, x2, y2, z2, 0f, uvOffset, light);
        vertex(vertexConsumer, pose, x3, y3, z3, PIXEL * 4, uvOffset, light);
        vertex(vertexConsumer, pose, x4, y4, z4, PIXEL * 4, 0f, light);
    }

    private static void drawCap(VertexConsumer vertexConsumer, PoseStack.Pose pose, int light,
            float y, float xOffset, float zOffset,
            float u1, float v1, float u2, float v2) {
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
