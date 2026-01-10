package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
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
    private static final float PIXEL = 1 / 16f;

    private static final int STAFF_GLOWING_LINE_HEIGHT_PIXELS = 1; //DO NOT FETCH FROM JSON
    private static final int STAFF_WIDTH_WORLD_PIXELS = 2;  // DO NOT FETCH
    private static final int STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS = 14; // TODO FETCH THIS FIELD FROM A JSON

    private static final double Z_OFFSET_MULTIPLIER = 1.01;

    // TODO fetch the following:
    private static final int STAFF_TEXTURE_HEIGHT = 64;
    private static final int STAFF_TEXTURE_WIDTH = 64;
    private static final QuadUV NORTH = new QuadUV(new IntPair(8, 4), new IntPair(12, 33));
    private static final QuadUV EAST = new QuadUV(new IntPair(8, 4), new IntPair(12, 33));
    private static final QuadUV WEST = new QuadUV(new IntPair(8, 4), new IntPair(12, 33));
    private static final QuadUV SOUTH = new QuadUV(new IntPair(8, 4), new IntPair(12, 33));
    private static final QuadUV PAW = new QuadUV(new IntPair(38, 41), new IntPair(64, 64));
    private static final QuadUV LINE = new QuadUV(new IntPair(16, 4), new IntPair(23, 4));

    private final GeoModel<T> model;

    private static ResourceLocation CAT_STAFF_TEXTURE;

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
                getTexture(animatable);
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

    private record Vec3Directions(Vec3 north, Vec3 east) {}

    public static void renderStaffInWorldSpace(PoseStack poseStack, MultiBufferSource bufferSource, int light, Vec3 staffOrigin, Vec3 staffTip, Direction pawDirection) {
        Vec3Directions directions = computeDirections(staffTip, staffOrigin);
        VertexConsumer normalVertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CAT_STAFF_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        renderBar(pose, normalVertexConsumer, light, staffOrigin, staffTip, directions);
        renderPaw(pose, normalVertexConsumer, staffTip, directions, pawDirection);
    }

    private void getTexture(T animatable) {
        ResourceLocation original = this.getTextureLocation(animatable);
        ResourceLocation result = original.withSuffix("_perch_travel");
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        try (AbstractTexture texture = textureManager.getTexture(original)) {
            NativeImage image;
            if (texture instanceof SimpleTexture simpleTexture) {
                image = simpleTexture.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
            } else if (texture instanceof DynamicTexture dynamicTexture) {
                image = dynamicTexture.getPixels();
            } else {
                throw new IllegalStateException("Invalid cat staff texture");
            }
            if (image != null) {
                Minecraft.getInstance().getTextureManager().register(result, new DynamicTexture(image));
                CAT_STAFF_TEXTURE = result;
            } else {
                throw new IllegalStateException("Invalid cat staff texture");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load cat staff texture", e);
        }
    }

    private static void renderBar(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, Vec3 staffOrigin, Vec3 staffTip, Vec3Directions directions) {
        Vec3 localNorth = directions.north();
        Vec3 localEast = directions.east();
        Vec3 originToTip = staffTip.subtract(staffOrigin);
        double totalLength = originToTip.length();
        Vec3 staffDirection = originToTip.normalize();
        double segmentLength = STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS * PIXEL;
        int segmentCount = (int) Math.floor(totalLength / segmentLength);

        int iteratedSegment = 0;
        while (iteratedSegment < segmentCount) {
            Vec3 segmentTip = staffTip.subtract(staffDirection.scale(iteratedSegment * segmentLength));
            Vec3 segmentOrigin = staffTip.subtract(staffDirection.scale((iteratedSegment + 1) * segmentLength));
            renderStaffSegment(pose, vertexConsumer, light, segmentOrigin, segmentTip, localNorth, localEast, 1);
            iteratedSegment++;
        }
        Vec3 segmentTip = staffTip.subtract(staffDirection.scale(iteratedSegment * segmentLength));
        float multiplier = (float) (segmentTip.subtract(staffOrigin).length() / segmentLength);
        renderStaffSegment(pose, vertexConsumer, light, staffOrigin, segmentTip, localNorth, localEast, multiplier);
    }

    private static void renderPaw(PoseStack.Pose pose, VertexConsumer vertexConsumer, Vec3 staffTip, Vec3Directions directions, Direction pawDirection) {
        Vec3 pawPosition = staffTip.subtract(0, STAFF_WIDTH_WORLD_PIXELS * PIXEL * 3, 0);
        Vec3 pawOrigin = pawPosition.subtract(0, STAFF_WIDTH_WORLD_PIXELS * PIXEL / 2, 0);
        Vec3 pawTip = pawPosition.add(0, STAFF_WIDTH_WORLD_PIXELS * PIXEL / 2, 0);

        Vec3 localNorth = directions.north();
        Vec3 localEast = directions.east();
        Vec3 localWest = localEast.scale(-1);
        Vec3 localSouth = localNorth.scale(-1);

        Vec3 primary = switch (pawDirection) {
            case NORTH -> localSouth;
            case EAST -> localWest;
            case SOUTH -> localNorth;
            case WEST -> localEast;
            default -> localNorth;
        };
        Vec3 secondary = primary == localNorth || primary == localSouth ? localEast : localNorth;
        primary = primary.scale(Z_OFFSET_MULTIPLIER);
        QuadCoords paw = buildSideQuad(pawOrigin, pawTip, primary, secondary);
        quad(vertexConsumer, pose, LightTexture.FULL_BRIGHT, PAW, paw);
    }

    private static void renderStaffSegment(
            PoseStack.Pose pose,
            VertexConsumer vertexConsumer,
            int light,
            Vec3 segmentOrigin,
            Vec3 segmentTip,
            Vec3 localNorth,
            Vec3 localEast,
            float heightMultiplier) {
        Vec3 localWest = localEast.scale(-1);
        Vec3 localSouth = localNorth.scale(-1);

        QuadCoords northSide = buildSideQuad(segmentOrigin, segmentTip, localNorth, localEast);
        QuadCoords southSide = buildSideQuad(segmentOrigin, segmentTip, localSouth, localWest);
        QuadCoords eastSide = buildSideQuad(segmentOrigin, segmentTip, localEast, localNorth);
        QuadCoords westSide = buildSideQuad(segmentOrigin, segmentTip, localWest, localSouth);

        quad(vertexConsumer, pose, light, NORTH, northSide, heightMultiplier);
        quad(vertexConsumer, pose, light, EAST, eastSide, heightMultiplier);
        quad(vertexConsumer, pose, light, SOUTH, southSide, heightMultiplier);
        quad(vertexConsumer, pose, light, WEST, westSide, heightMultiplier);
    }

    private static Vec3Directions computeDirections(Vec3 staffTip, Vec3 staffOrigin) {
        Vec3 axis = staffTip.subtract(staffOrigin).normalize();

        Vec3 referenceUp = Math.abs(axis.dot(MineraculousMathUtils.UP)) > 0.99
                ? MineraculousMathUtils.EAST
                : MineraculousMathUtils.UP;

        Vec3 localNorth = axis.cross(referenceUp).normalize();
        Vec3 localEast = localNorth.cross(axis).normalize();

        double halfWidth = STAFF_WIDTH_WORLD_PIXELS * PIXEL / 2;
        localNorth = localNorth.scale(halfWidth);
        localEast = localEast.scale(halfWidth);

        return new Vec3Directions(localNorth, localEast);
    }

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

    private static void quad(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            int light,
            QuadUV uv,
            QuadCoords coords) {
        quad(vertexConsumer, pose, light, uv, coords, 1);
    }

    private static void quad(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            int light,
            QuadUV uv,
            QuadCoords coords,
            float heightMultiplier) {
        float down = (float) uv.downRightUV.second / STAFF_TEXTURE_HEIGHT * heightMultiplier;
        float right = (float) uv.downRightUV.first / STAFF_TEXTURE_WIDTH;
        float up = (float) uv.upLeftUV.second / STAFF_TEXTURE_HEIGHT;
        float left = (float) uv.upLeftUV.first / STAFF_TEXTURE_WIDTH;
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.upLeft.x, (float) coords.upLeft.y, (float) coords.upLeft.z, left, up, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.upRight.x, (float) coords.upRight.y, (float) coords.upRight.z, right, up, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.downRight.x, (float) coords.downRight.y, (float) coords.downRight.z, right, down, light);
        MineraculousClientUtils.vertex(vertexConsumer, pose, (float) coords.downLeft.x, (float) coords.downLeft.y, (float) coords.downLeft.z, left, down, light);
    }
}
