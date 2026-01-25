package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
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
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchGroundWorker;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffTravelGroundWorker;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
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
import org.joml.Quaternionf;
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
    private static final QuadUV CAP = new QuadUV(new IntPair(4, 3), new IntPair(8, 4));

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
                    if (perchingCatStaffData.isModeActive() || travelingCatStaffData.isModeActive()) {
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

    private static ResourceLocation getWorldTexture(ItemStack stack) {
        Look<?> look = MiraculousItemRenderer.getLook(stack, LookContexts.MIRACULOUS_TOOL);
        if (look == null)
            look = LookManager.getBuiltInLook(MiraculousToolLookRenderer.getDefaultLookId(stack));
        ResourceLocation original = look.getAsset(LookContexts.MIRACULOUS_TOOL, LookAssetTypes.TEXTURE);
        if (original == null) {
            return MissingTextureAtlasSprite.getLocation();
        }
        ResourceLocation result = original.withSuffix("_perch_travel");
        if (Minecraft.getInstance().getTextureManager().getTexture(result) == MissingTextureAtlasSprite.getTexture())
            registerWorldTextures(original, result);
        return result;
    }

    private static void registerWorldTextures(ResourceLocation original, ResourceLocation result) {
        try (AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(original)) {
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
                // TODO: Glowmask too if present
                // (i dont think this to do is needed)
            } else {
                throw new IllegalStateException("Invalid cat staff texture");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load cat staff texture", e);
        }
    }

    private record IntPair(int first, int second) {}

    private record QuadUV(IntPair upLeftUV, IntPair downRightUV) {}

    private record QuadCoords(Vec3 downLeft, Vec3 downRight, Vec3 upRight, Vec3 upLeft) {}

    private record Vec3Directions(Vec3 north, Vec3 east) {}

    public static void renderPerchInWorldSpace(PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick, Entity entity, PerchingCatStaffData perchingData) {
        Vec3 origin = perchingData.staffOrigin();
        double interpolatedLength = CatStaffPerchGroundWorker.expectedStaffTip(entity, partialTick).y - origin.y;
        boolean leaning = perchingData.state() == PerchingCatStaffData.PerchingState.LEAN;
        if (leaning) {
            Vec3 userGroundProjected = perchingData.userPositionBeforeLeanOrRelease().multiply(1, 0, 1).add(0, perchingData.staffOrigin().y, 0);
            Vec3 userPosition = entity.getPosition(partialTick);
            Quaternionf rotation = new Quaternionf().rotationTo(MineraculousMathUtils.UP.toVector3f(), userPosition.subtract(userGroundProjected).normalize().toVector3f());
            poseStack.pushPose();
            poseStack.rotateAround(
                    rotation,
                    (float) perchingData.staffOrigin().x,
                    (float) perchingData.staffOrigin().y,
                    (float) perchingData.staffOrigin().z);
        }
        CatStaffRenderer.renderStaffInWorldSpace(perchingData.stack(), poseStack, bufferSource, light, origin, false, interpolatedLength, perchingData.pawDirection());
        if (leaning) {
            poseStack.popPose();
        }
    }

    public static void renderTravelInWorldSpace(PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick, Entity entity, TravelingCatStaffData travelingData) {
        double length = travelingData.staffOrigin().subtract(travelingData.staffTip()).length();
        Vec3 tipToOrigin = travelingData.staffOrigin().subtract(travelingData.staffTip());
        Vec3 interpolatedTip = CatStaffTravelGroundWorker.expectedStaffTip(entity, travelingData, partialTick);

        Quaternionf rotation = new Quaternionf().rotationTo(MineraculousMathUtils.UP.scale(-1).toVector3f(), tipToOrigin.normalize().toVector3f());
        poseStack.pushPose();
        poseStack.rotateAround(
                rotation,
                (float) interpolatedTip.x,
                (float) interpolatedTip.y,
                (float) interpolatedTip.z);
        Direction pawDirection = Direction.getNearest(travelingData.initialUserHorizontalDirection());
        CatStaffRenderer.renderStaffInWorldSpace(travelingData.stack(), poseStack, bufferSource, light, interpolatedTip, true, length, pawDirection);
        poseStack.popPose();
    }

    public static void renderStaffInWorldSpace(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, Vec3 staffExtremity, boolean tip, double length, Direction pawDirection) {
        //Vec3 staffOrigin = staffTip.add(0, -length, 0);
        Vec3 staffTip, staffOrigin;
        if (tip) {
            staffTip = staffExtremity;
            staffOrigin = staffTip.add(0, -length, 0);
        } else {
            staffOrigin = staffExtremity;
            staffTip = staffOrigin.add(0, length, 0);
        }
        Vec3Directions directions = computeDirections(staffTip, staffOrigin);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getWorldTexture(stack)));
        PoseStack.Pose pose = poseStack.last();
        renderBar(pose, vertexConsumer, light, staffOrigin, staffTip, directions);
        renderPaw(pose, vertexConsumer, staffTip, directions, pawDirection);
        renderLines(pose, vertexConsumer, staffTip, directions);
    }

    private static void renderBar(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, Vec3 staffOrigin, Vec3 staffTip, Vec3Directions directions) {
        Vec3 originToTip = staffTip.subtract(staffOrigin);
        double totalLength = originToTip.length();
        Vec3 staffDirection = originToTip.normalize();
        double segmentLength = STAFF_SELECTED_UV_HEIGHT_WORLD_PIXELS * PIXEL;
        int segmentCount = (int) Math.floor(totalLength / segmentLength);

        int iteratedSegment = 0;
        while (iteratedSegment < segmentCount) {
            Vec3 segmentTip = staffTip.subtract(staffDirection.scale(iteratedSegment * segmentLength));
            Vec3 segmentOrigin = staffTip.subtract(staffDirection.scale((iteratedSegment + 1) * segmentLength));
            renderStaffSegment(pose, vertexConsumer, light, segmentOrigin, segmentTip, directions, 1);
            iteratedSegment++;
        }
        Vec3 segmentTip = staffTip.subtract(staffDirection.scale(iteratedSegment * segmentLength));
        float multiplier = (float) (segmentTip.subtract(staffOrigin).length() / segmentLength);
        renderStaffSegment(pose, vertexConsumer, light, staffOrigin, segmentTip, directions, multiplier);
        renderCaps(pose, vertexConsumer, light, staffOrigin, staffTip, directions);
    }

    private static void renderPaw(PoseStack.Pose pose, VertexConsumer vertexConsumer, Vec3 staffTip, Vec3Directions directions, Direction pawDirection) {
        Vec3 pawPosition = getPawPosition(staffTip);
        double halfPawHeight = STAFF_WIDTH_WORLD_PIXELS * PIXEL / 2;
        Vec3 pawOrigin = pawPosition.subtract(0, halfPawHeight, 0);
        Vec3 pawTip = pawPosition.add(0, halfPawHeight, 0);

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

    private static Vec3 getPawPosition(Vec3 staffTip) {
        return staffTip.subtract(0, STAFF_WIDTH_WORLD_PIXELS * PIXEL * 4, 0);
    }

    private static void renderLines(PoseStack.Pose pose, VertexConsumer vertexConsumer, Vec3 staffTip, Vec3Directions directions) {
        Vec3 pawPosition = getPawPosition(staffTip);
        double offset = (STAFF_WIDTH_WORLD_PIXELS + 3.4) * PIXEL / 2.0;
        double thirdLineOffset = 7 / 10.0;
        Vec3 firstLine = pawPosition.add(0, offset, 0);
        Vec3 secondLine = pawPosition.subtract(0, offset, 0);
        Vec3 thirdLine = secondLine.add(secondLine.subtract(firstLine).scale(thirdLineOffset));

        renderLine(pose, vertexConsumer, firstLine, directions);
        renderLine(pose, vertexConsumer, secondLine, directions);
        renderLine(pose, vertexConsumer, thirdLine, directions);
    }

    private static void renderLine(PoseStack.Pose pose, VertexConsumer vertexConsumer, Vec3 linePosition, Vec3Directions directions) {
        Vec3 localNorth = directions.north();
        Vec3 localEast = directions.east();
        Vec3 localWest = localEast.scale(-1);
        Vec3 localSouth = localNorth.scale(-1);

        Vec3 lineTop = linePosition.add(0, STAFF_GLOWING_LINE_HEIGHT_PIXELS * PIXEL / 4.0, 0);
        Vec3 lineBottom = linePosition.subtract(0, STAFF_GLOWING_LINE_HEIGHT_PIXELS * PIXEL / 4.0, 0);

        QuadCoords northSide = buildSideQuad(lineBottom, lineTop, localNorth.scale(Z_OFFSET_MULTIPLIER), localEast);
        QuadCoords southSide = buildSideQuad(lineBottom, lineTop, localSouth.scale(Z_OFFSET_MULTIPLIER), localWest);
        QuadCoords eastSide = buildSideQuad(lineBottom, lineTop, localEast.scale(Z_OFFSET_MULTIPLIER), localNorth);
        QuadCoords westSide = buildSideQuad(lineBottom, lineTop, localWest.scale(Z_OFFSET_MULTIPLIER), localSouth);

        quad(vertexConsumer, pose, LightTexture.FULL_BRIGHT, LINE, northSide);
        quad(vertexConsumer, pose, LightTexture.FULL_BRIGHT, LINE, southSide);
        quad(vertexConsumer, pose, LightTexture.FULL_BRIGHT, LINE, eastSide);
        quad(vertexConsumer, pose, LightTexture.FULL_BRIGHT, LINE, westSide);
    }

    private static void renderStaffSegment(
            PoseStack.Pose pose,
            VertexConsumer vertexConsumer,
            int light,
            Vec3 segmentOrigin,
            Vec3 segmentTip,
            Vec3Directions directions,
            float heightMultiplier) {
        Vec3 localNorth = directions.north();
        Vec3 localEast = directions.east();
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

    private static void renderCaps(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, Vec3 staffOrigin, Vec3 staffTip, Vec3Directions directions) {
        Vec3 localNorth = directions.north();
        Vec3 localEast = directions.east();
        QuadCoords upSide = buildCapQuad(staffTip, localNorth, localEast);
        QuadCoords downSide = buildCapQuad(staffOrigin, localNorth, localEast);
        quad(vertexConsumer, pose, light, CAP, upSide);
        quad(vertexConsumer, pose, light, CAP, downSide);
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

    private static QuadCoords buildCapQuad(
            Vec3 position,
            Vec3 halfLength,
            Vec3 halfWidth) {
        return new QuadCoords(
                position.add(halfLength).subtract(halfWidth),
                position.add(halfLength).add(halfWidth),
                position.subtract(halfLength).add(halfWidth),
                position.subtract(halfLength).subtract(halfWidth));
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
