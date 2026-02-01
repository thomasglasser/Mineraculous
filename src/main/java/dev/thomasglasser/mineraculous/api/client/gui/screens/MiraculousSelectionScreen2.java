package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

public class MiraculousSelectionScreen2 extends MiraculousSelecting {
    private static final ResourceLocation CIRCLE_TEXTURE = MineraculousConstants.modLoc("textures/gui/sprites/miraculous_selection/circle.png");
    private static final float MAX_CIRCLE_RADIUS = 1;

    protected final int activationKey;
    private final Map<ResourceKey<Miraculous>, ItemStack> availableMiraculous = new LinkedHashMap<>();

    private float oldCircleRadius = 0;
    private float circleRadius = 0;
    private float wheelRotationAngle = 0;
    private float oldWheelRotationAngle = 0;
    private int selectedOptionIndex = 0;

    public MiraculousSelectionScreen2(int activationKey) {
        //super(Component.empty());
        this.activationKey = activationKey;
        updateAvailableMiraculous();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == activationKey) {
            onClose();
            return true;
        }
        updateSelectedOption(keyCode);
        updateMiraculousPoweredState();
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        updateAvailableMiraculous();
        if (availableMiraculous.isEmpty()) {
            this.onClose();
        }
        oldCircleRadius = circleRadius;
        if (circleRadius < MAX_CIRCLE_RADIUS) {
            circleRadius += 0.1f;
        }
        oldWheelRotationAngle = wheelRotationAngle;
        float delta = getWheelTargetAngle() - wheelRotationAngle;
        delta = (float) Math.atan2(Math.sin(delta), Math.cos(delta));
        wheelRotationAngle += delta * 0.25f;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        MultiBufferSource bufferSource = guiGraphics.bufferSource();
        int height = guiGraphics.guiHeight();
        int width = guiGraphics.guiWidth();
    }

    @Override
    public void render3dElements(
            RenderLevelStageEvent.Stage stage,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float partialTick) {
        Vec3 lookAngle = Minecraft.getInstance().player.getLookAngle();
        RenderSystem.setupLevelDiffuseLighting(lookAngle.toVector3f(), lookAngle.scale(-1).toVector3f());
        if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            renderMiraculous(poseStack, bufferSource, partialTick);
        }
        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            renderGoldenCircle(poseStack, bufferSource, partialTick);
        }
    }

    private void renderGoldenCircle(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.text(CIRCLE_TEXTURE));
        poseStack.pushPose();
        applyGlobalTransforms(poseStack, partialTick);
        PoseStack.Pose pose = poseStack.last();
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(-1, 1, 0), 0, 0, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(1, 1, 0), 1, 0, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(1, -1, 0), 1, 1, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(-1, -1, 0), 0, 1, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private void renderMiraculous(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        if (availableMiraculous.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        applyGlobalTransforms(poseStack, partialTick);
        double angleStep = getAngleStep();
        int i = 0;
        for (ItemStack miraculous : availableMiraculous.values()) {
            double angle = i * angleStep;
            double x = -Math.cos(angle) * 0.89;
            double y = Math.sin(angle) * 0.89;
            poseStack.pushPose();
            poseStack.translate(x, y, -0.4);
            poseStack.scale(3f, 3f, 3f);
            poseStack.mulPose(Axis.ZN.rotation((float) angle));
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    miraculous,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    0,
                    poseStack,
                    bufferSource,
                    null,
                    0);
            poseStack.popPose();
            i++;
        }
        poseStack.pushPose();
        double angle = i * angleStep;
        double x = -Math.cos(angle) * 0.89;
        double y = Math.sin(angle) * 0.89;
        poseStack.pushPose();
        poseStack.translate(x, y, -0.4);
        poseStack.scale(0.3f, 0.3f, 0.3f);
        poseStack.mulPose(Axis.ZN.rotation((float) angle));
        poseStack.mulPose(Axis.XN.rotationDegrees(90));
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                Items.BARRIER.getDefaultInstance(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                0,
                poseStack,
                bufferSource,
                null,
                0);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose(); //TODO FIX barrier
    }

    private void applyGlobalTransforms(PoseStack poseStack, float partialTick) {
        MineraculousClientUtils.rotateFacingCamera(poseStack, new Vector3f(0, 0, 0), 0);
        float scale = 0.089f;
        float wheelSize = getCircleRadius(partialTick);
        float angleStep = getAngleStep();
        float lastAngleOption = (float) (availableMiraculous.size() * angleStep + Math.PI / 2);
        float interpolatedAngle = Mth.lerp(partialTick, oldWheelRotationAngle, wheelRotationAngle);
        poseStack.translate(0, -0.05, 0.24);
        poseStack.scale(wheelSize, wheelSize, wheelSize);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(89));
        poseStack.mulPose(Axis.ZP.rotation(lastAngleOption));
        poseStack.mulPose(Axis.ZP.rotation(interpolatedAngle));
    }

    private float getCircleRadius(float partialTick) {
        return Mth.lerp(partialTick, oldCircleRadius, circleRadius);
    }

    private void updateAvailableMiraculous() {
        Set<ResourceKey<Miraculous>> updated = new HashSet<>();
        for (ItemStack stack : CuriosUtils.getAllItems(ClientUtils.getLocalPlayer()).values()) {
            Holder<Miraculous> holder = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (holder == null || holder.getKey() == null) continue;
            ResourceKey<Miraculous> key = holder.getKey();
            updated.add(key);
            if (!availableMiraculous.containsKey(key)) {
                ItemStack copy = stack.copy();
                copy.set(
                        MineraculousDataComponents.POWER_STATE,
                        MiraculousItem.PowerState.HIDDEN);
                availableMiraculous.put(key, copy);
            }
        }
        availableMiraculous.keySet().removeIf(key -> !updated.contains(key));
    }

    private void updateSelectedOption(int keyCode) {
        boolean left = keyCode == Minecraft.getInstance().options.keyLeft.getKey().getValue();
        boolean right = keyCode == Minecraft.getInstance().options.keyRight.getKey().getValue();
        if (!(left && right) && (left || right)) {
            selectedOptionIndex += right ? 1 : -1;
            selectedOptionIndex = selectedOptionIndex > availableMiraculous.size() ? 0 : selectedOptionIndex;
            selectedOptionIndex = selectedOptionIndex < 0 ? availableMiraculous.size() : selectedOptionIndex;
        }
    }

    private void updateMiraculousPoweredState() {
        List<Map.Entry<ResourceKey<Miraculous>, ItemStack>> entries = new ArrayList<>(availableMiraculous.entrySet());
        for (ItemStack stack : availableMiraculous.values()) {
            stack.set(
                    MineraculousDataComponents.POWER_STATE,
                    MiraculousItem.PowerState.HIDDEN);
        }
        if (isAnyMiraculousSelected()) {
            ResourceKey<Miraculous> key = entries.get(selectedOptionIndex - 1).getKey();
            ItemStack selectedStack = availableMiraculous.get(key);
            selectedStack.set(
                    MineraculousDataComponents.POWER_STATE,
                    MiraculousItem.PowerState.POWERED);
        }
    }

    private boolean isAnyMiraculousSelected() {
        return selectedOptionIndex != 0;
    }

    private float getWheelTargetAngle() {
        return selectedOptionIndex * getAngleStep();
    }

    private float getAngleStep() {
        return (float) ((Math.PI * 2) / (availableMiraculous.size() + 1));
    }
}
