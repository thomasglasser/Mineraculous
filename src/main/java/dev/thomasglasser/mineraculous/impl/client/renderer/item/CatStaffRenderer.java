package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.util.renderer.MiraculousToolLookRenderer;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CatStaffRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> implements MiraculousToolLookRenderer {
    public static final ResourceLocation EXTENDED_LOCATION = MineraculousConstants.modLoc("textures/item/geo/cat_staff.png");
    private static final float PIXEL = 1 / 16f;

    private static final int STAFF_GLOWING_LINE_HEIGHT_PIXELS = 1; //DO NOT FETCH FROM JSON
    private static final int STAFF_WIDTH_WORLD_PIXELS = 2;  // DO NOT FETCH

    private static final int STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS = 14; // TODO FETCH THIS FIELD FROM A JSON
    private static final IntPair STAFF_SIDE_WORLD_DIMENSION_PIXELS = new IntPair(STAFF_WIDTH_WORLD_PIXELS, STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS); // DO NOT FETCH
    private static final IntPair STAFF_PAW_WORLD_DIMENSION_PIXELS = new IntPair(STAFF_WIDTH_WORLD_PIXELS, STAFF_WIDTH_WORLD_PIXELS); // DO NOT FETCH
    private static final IntPair STAFF_LINE_WORLD_DIMENSION_PIXELS = new IntPair(STAFF_WIDTH_WORLD_PIXELS, STAFF_GLOWING_LINE_HEIGHT_PIXELS); // DO NOT FETCH

    // TODO fetch the following:
    private static final IntPair STAFF_TEXTURE_SIZE_PIXELS = new IntPair(64, 64);
    private static final QuadUV NORTH = new QuadUV(new IntPair(0, 4), new IntPair(4, 33));
    private static final QuadUV EAST = new QuadUV(new IntPair(0, 4), new IntPair(4, 33));
    private static final QuadUV WEST = new QuadUV(new IntPair(0, 4), new IntPair(4, 33));
    private static final QuadUV SOUTH = new QuadUV(new IntPair(0, 4), new IntPair(4, 33));
    private static final QuadUV PAW = new QuadUV(new IntPair(38, 41), new IntPair(63, 63));
    private static final QuadUV LINE = new QuadUV(new IntPair(16, 4), new IntPair(23, 4));

    private final GeoModel<T> model;

    public CatStaffRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        this.model = new LookGeoModel<>(this);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    @Override
    public Holder<LookContext> getContext() {
        ItemStack stack = getCurrentItemStack();
        if (stack.has(MineraculousDataComponents.BLOCKING))
            return LookContexts.BLOCKING_MIRACULOUS_TOOL;
        return switch (stack.get(MineraculousDataComponents.CAT_STAFF_MODE)) {
            case PHONE -> LookContexts.PHONE_MIRACULOUS_TOOL;
            case SPYGLASS -> LookContexts.SPYGLASS_MIRACULOUS_TOOL;
            case null, default -> LookContexts.MIRACULOUS_TOOL;
        };
    }

    @Override
    public void defaultRender(PoseStack poseStack, T animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        ItemStack stack = getCurrentItemStack();
        Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
        Level level = ClientUtils.getLevel();
        if (carrierId != null && level != null) {
            Entity carrier = level.getEntity(carrierId);
            if (carrier != null) {
                CatStaffItem.Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
                if (mode == CatStaffItem.Mode.PERCH || mode == CatStaffItem.Mode.TRAVEL) {
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
        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    private record IntPair(int first, int second) {}

    private record QuadUV(IntPair upLeftUV, IntPair downRightUV) {}

    private record QuadCoords(Vec3 downLeft, Vec3 downRight, Vec3 upRight, Vec3 upLeft) {}

    private static QuadCoords buildSideQuad(
            Vec3 origin,
            Vec3 tip,
            Vec3 normal,
            Vec3 halfWidth) {
        return new QuadCoords(
                origin.add(normal).add(halfWidth),
                origin.add(normal).subtract(halfWidth),
                tip.add(normal).subtract(halfWidth),
                tip.add(normal).add(halfWidth));
    }

    public static void renderStaffInWorldSpace(PoseStack poseStack, MultiBufferSource bufferSource, int light, Vec3 staffOrigin, Vec3 staffTip) {
        Vec3 originToTip = staffTip.subtract(staffOrigin);
        double totalLength = originToTip.length();
        Vec3 direction = originToTip.normalize();

        double segmentLength = STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS * PIXEL;
        int segmentCount = (int) Math.floor(totalLength / segmentLength);

        for (int i = 0; i < segmentCount; i++) {
            Vec3 segmentTip = staffTip.subtract(direction.scale(i * segmentLength));
            Vec3 segmentOrigin = staffTip.subtract(direction.scale((i + 1) * segmentLength));

            renderStaffSegment(
                    poseStack,
                    bufferSource,
                    light,
                    segmentOrigin,
                    segmentTip);
        }
    }

    private static void renderStaffSegment(PoseStack poseStack, MultiBufferSource bufferSource, int light, Vec3 segmentOrigin, Vec3 segmentTip) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(EXTENDED_LOCATION));

        Vec3 axis = segmentTip.subtract(segmentOrigin).normalize();

        Vec3 referenceUp = Math.abs(axis.dot(MineraculousMathUtils.UP)) > 0.99
                ? MineraculousMathUtils.EAST
                : MineraculousMathUtils.UP;

        Vec3 localEast = axis.cross(referenceUp).normalize();
        Vec3 localNorth = localEast.cross(axis).normalize();

        double halfWidth = STAFF_WIDTH_WORLD_PIXELS * PIXEL / 2;
        localEast = localEast.scale(halfWidth);
        localNorth = localNorth.scale(halfWidth);

        Vec3 localWest = localEast.scale(-1);
        Vec3 localSouth = localNorth.scale(-1);

        QuadCoords northSide = buildSideQuad(segmentOrigin, segmentTip, localNorth, localEast);
        QuadCoords southSide = buildSideQuad(segmentOrigin, segmentTip, localSouth, localEast);
        QuadCoords eastSide = buildSideQuad(segmentOrigin, segmentTip, localEast, localNorth);
        QuadCoords westSide = buildSideQuad(segmentOrigin, segmentTip, localWest, localNorth);

        PoseStack.Pose pose = poseStack.last();
        quad(vertexConsumer, pose, light, NORTH, northSide);
        quad(vertexConsumer, pose, light, EAST, eastSide);
        quad(vertexConsumer, pose, light, SOUTH, southSide);
        quad(vertexConsumer, pose, light, WEST, westSide);
    }

    private static void quad(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            int light,
            QuadUV uv,
            QuadCoords coords) {
        float down = (float) uv.downRightUV.second / STAFF_TEXTURE_SIZE_PIXELS.first;
        float right = (float) uv.upLeftUV.first / STAFF_TEXTURE_SIZE_PIXELS.first;
        float up = (float) uv.upLeftUV.second / STAFF_TEXTURE_SIZE_PIXELS.first;
        float left = (float) uv.downRightUV.first / STAFF_TEXTURE_SIZE_PIXELS.first;

        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.upLeft.x, (float) coords.upLeft.y, (float) coords.upLeft.z, left, up, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.upRight.x, (float) coords.upRight.y, (float) coords.upRight.z, right, up, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.downRight.x, (float) coords.downRight.y, (float) coords.downRight.z, right, down, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.downLeft.x, (float) coords.downLeft.y, (float) coords.downLeft.z, left, down, light);
    }
}
