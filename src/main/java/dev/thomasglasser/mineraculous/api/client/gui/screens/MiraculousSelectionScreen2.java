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
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.HashSet;
import java.util.Iterator;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

public class MiraculousSelectionScreen2 extends MiraculousSelecting {
    private static final ResourceLocation PARTICLE_TEXTURE = MineraculousConstants.modLoc("textures/gui/sprites/miraculous_selection/particle.png");
    private static final float MAX_CIRCLE_RADIUS = 1;
    private static final float MAX_VERTICAL_OFFSET = 0.06f;
    private static final float VERTICAL_OFFSET_SPEED = 0.003f;

    protected final int activationKey;
    private final Map<ResourceKey<Miraculous>, MiraculousOptionData> availableMiraculous = new LinkedHashMap<>();

    private float oldCircleRadius = 0;
    private float circleRadius = 0;
    private float wheelRotationAngle = 0;
    private float oldWheelRotationAngle = 0;
    private float targetWheelRotationAngle = 0;
    private int selectedOptionIndex = 0;
    private float verticalOffset = 0;
    private boolean verticalOffsetIncreasing = true;
    private int currentParticleColor = 0xFFFFFFFF;
    private int targetParticleColor = 0xFFFFFFFF;
    private float angleStep = 0f;
    private float oldAngleStep = 0f;
    private float targetAngleStep = 0f;

    private Set<ParticleQuad> particles = new HashSet<>();

    public MiraculousSelectionScreen2(int activationKey) {
        this.activationKey = activationKey;
        updateAvailableMiraculous();
        updateMiraculousPoweredState();
        createParticles();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Map.Entry<ResourceKey<Miraculous>, MiraculousOptionData>> entries = getUnchosenEntries();
        if (!entries.isEmpty()) {
            Map.Entry<ResourceKey<Miraculous>, MiraculousOptionData> selectedEntry = entries.get(selectedOptionIndex);
            ResourceKey<Miraculous> key = selectedEntry.getKey();
            MiraculousOptionData data = selectedEntry.getValue();
            availableMiraculous.put(
                    key,
                    new MiraculousOptionData(
                            data.stack(),
                            true,
                            data.verticalOffset(),
                            data.oldVerticalOffset()));
            int count = availableMiraculous.size() - getChosenCount();
            selectedOptionIndex = Mth.clamp(selectedOptionIndex, 0, Math.max(0, count - 1));
            updateMiraculousPoweredState();
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        updateAngleStep();
        updateWheelRotation();
        updateMiraculousVerticalOffsets();
        updateParticleColor();
        for (ParticleQuad particle : particles) {
            particle.tick();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        MultiBufferSource bufferSource = guiGraphics.bufferSource();
        int height = guiGraphics.guiHeight();
        int width = guiGraphics.guiWidth();
        String text = "Selected:";
        int xOffset = Minecraft.getInstance().font.width(text) / 2;
        int yOffset = Minecraft.getInstance().font.lineHeight;
        if (getChosenCount() != 0) {
            guiGraphics.drawString(Minecraft.getInstance().font, text, width / 2 - xOffset, yOffset, 0xFFFFFFFF);
        }
        int chosenCount = getChosenCount();
        int slotSize = 30;
        final int itemWidth = 32;
        int totalLength = chosenCount * slotSize;

        int currentSlot = 1; // range: (0, chosenCount]
        poseStack.pushPose();
        poseStack.scale(2, 2, 2);
        for (MiraculousOptionData option : availableMiraculous.values()) {
            if (option.chosen()) {
                int currentSlotPosition = width / 2 - totalLength / 2 + (currentSlot - 1) * slotSize;

                guiGraphics.renderItem(option.stack, (currentSlotPosition + slotSize / 2 - itemWidth / 2) / 2, (yOffset * 3) / 2);

                currentSlot++;
            }
        }
        poseStack.popPose();
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
            renderMiraculousAboveParticles(poseStack, bufferSource, partialTick);
        }
        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            renderParticleSurface(poseStack, bufferSource, partialTick);
        }
    }

    private void renderParticleSurface(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        poseStack.pushPose();
        applyParticlesTransforms(poseStack, partialTick);
        for (ParticleQuad particle : particles) {
            particle.render(bufferSource, poseStack, partialTick, currentParticleColor);
        }
        poseStack.popPose();
    }

    private void renderMiraculousAboveParticles(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        if (availableMiraculous.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        applyMiraculousTransforms(poseStack, partialTick);
        int optionCount = 0;
        for (MiraculousOptionData option : availableMiraculous.values()) {
            if (!option.chosen) {
                poseStack.pushPose();
                poseStack.translate(0, 0, Mth.lerp(partialTick, option.oldVerticalOffset(), option.verticalOffset()));
                applyMiraculousLocalTransforms(poseStack, optionCount * getAngleStep(partialTick));
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        option.stack(),
                        ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT,
                        0,
                        poseStack,
                        bufferSource,
                        null,
                        0);
                poseStack.popPose();
                optionCount++;
            }
        }
        poseStack.popPose();
    }

    private void applyParticlesTransforms(PoseStack poseStack, float partialTick) {
        final float SCALE = 0.006f;
        final double Y_TRANSLATION = -7;
        final double Z_TRANSLATION = 35;
        final float WIDTH_DISTORTION_FACTOR = 1.05f;
        float wheelSize = getCircleRadius(partialTick);
        MineraculousClientUtils.rotateFacingCamera(poseStack, new Vector3f(0, 0, 0), 0);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0, Y_TRANSLATION, Z_TRANSLATION);
        poseStack.scale(wheelSize * WIDTH_DISTORTION_FACTOR, wheelSize, wheelSize * WIDTH_DISTORTION_FACTOR);
    }

    private void applyMiraculousTransforms(PoseStack poseStack, float partialTick) {
        final float SCALE = 0.089f;
        final float TILT_DEGREES = 89;
        final double Y_TRANSLATION = -0.05;
        final double Z_TRANSLATION = 0.24;
        float wheelSize = getCircleRadius(partialTick);
        float angleStep = getAngleStep(partialTick);
        float lastAngleOption = (float) ((availableMiraculous.size() - getChosenCount()) * angleStep + Math.PI / 2);
        float interpolatedAngle = Mth.lerp(partialTick, oldWheelRotationAngle, wheelRotationAngle);
        MineraculousClientUtils.rotateFacingCamera(poseStack, new Vector3f(0, 0, 0), 0);
        poseStack.translate(0, Y_TRANSLATION, Z_TRANSLATION);
        poseStack.scale(wheelSize, wheelSize, wheelSize);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT_DEGREES));
        poseStack.mulPose(Axis.ZP.rotation(lastAngleOption));
        poseStack.mulPose(Axis.ZP.rotation(interpolatedAngle));
    }

    private void applyMiraculousLocalTransforms(PoseStack poseStack, double angle) {
        final double RADIUS = 1.16;
        final double Z_TRANSLATION = -0.4;
        final float SCALE = 3f;
        double x = -Math.cos(angle) * RADIUS;
        double y = Math.sin(angle) * RADIUS;
        poseStack.translate(x, y, Z_TRANSLATION);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(Axis.ZN.rotation((float) angle));
        poseStack.mulPose(Axis.XN.rotationDegrees(90));
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.mulPose(Axis.XP.rotationDegrees(5));
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
                boolean transformed = stack.get(MineraculousDataComponents.POWER_STATE) != MiraculousItem.PowerState.ACTIVE &&
                        stack.get(MineraculousDataComponents.POWER_STATE) != MiraculousItem.PowerState.HIDDEN;
                if (transformed) {
                    copy.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
                } else {
                    copy.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.HIDDEN);
                }
                availableMiraculous.put(key, new MiraculousOptionData(copy, false, 0, 0));
            }
        }
        availableMiraculous.keySet().removeIf(key -> !updated.contains(key));
    }

    private void updateSelectedOption(int keyCode) {
        boolean left = keyCode == Minecraft.getInstance().options.keyLeft.getKey().getValue();
        boolean right = keyCode == Minecraft.getInstance().options.keyRight.getKey().getValue();
        if (left ^ right) {
            int count = availableMiraculous.size() - getChosenCount();
            if (count <= 0) return;
            int direction = right ? 1 : -1;
            targetWheelRotationAngle += direction * targetAngleStep;
            selectedOptionIndex += direction;
            if (selectedOptionIndex >= count) {
                selectedOptionIndex = 0;
            } else if (selectedOptionIndex < 0) {
                selectedOptionIndex = count - 1;
            }
        }
    }

    private void updateMiraculousPoweredState() {
        for (MiraculousOptionData option : availableMiraculous.values()) {
            option.stack().set(
                    MineraculousDataComponents.POWER_STATE,
                    option.chosen()
                            ? MiraculousItem.PowerState.POWERED
                            : MiraculousItem.PowerState.HIDDEN);
        }
        List<Map.Entry<ResourceKey<Miraculous>, MiraculousOptionData>> entries = getUnchosenEntries();
        if (entries.isEmpty()) {
            targetParticleColor = 0xFFFFFFFF;
        } else {
            ResourceKey<Miraculous> key = entries.get(selectedOptionIndex).getKey();
            ItemStack selectedStack = availableMiraculous.get(key).stack();
            selectedStack.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
            targetParticleColor = selectedStack.get(MineraculousDataComponents.MIRACULOUS)
                    .value()
                    .color()
                    .getValue()
                    | 0xFF000000;
        }
    }

    private void updateParticleColor() {
        final float TRANSITION_SPEED = 0.3f;
        currentParticleColor = MineraculousMathUtils.lerpColor(
                currentParticleColor,
                targetParticleColor,
                TRANSITION_SPEED);
    }

    private void updateMiraculousVerticalOffsets() {
        int direction = verticalOffsetIncreasing ? 1 : -1;
        verticalOffset += VERTICAL_OFFSET_SPEED * direction;
        if (verticalOffset >= MAX_VERTICAL_OFFSET) {
            verticalOffset = MAX_VERTICAL_OFFSET;
            verticalOffsetIncreasing = false;
        } else if (verticalOffset <= -MAX_VERTICAL_OFFSET) {
            verticalOffset = -MAX_VERTICAL_OFFSET;
            verticalOffsetIncreasing = true;
        }
        Iterator<ResourceKey<Miraculous>> iterator = availableMiraculous.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            ResourceKey<Miraculous> miraculous = iterator.next();
            int localDirection = i % 2 == 0 ? 1 : -1;
            float newOffset = verticalOffset * localDirection;
            availableMiraculous.computeIfPresent(miraculous, (k, data) -> data.withVerticalOffset(newOffset));
            i++;
        }
    }

    private int getChosenCount() {
        return Math.toIntExact(availableMiraculous.values().stream()
                .filter(MiraculousOptionData::chosen)
                .count());
    }

    private List<Map.Entry<ResourceKey<Miraculous>, MiraculousOptionData>> getUnchosenEntries() {
        return availableMiraculous.entrySet().stream()
                .filter(entry -> !entry.getValue().chosen())
                .toList();
    }

    private float getAngleStep(float partialTick) {
        return Mth.lerp(partialTick, oldAngleStep, angleStep);
    }

    private void updateAngleStep() {
        int unchosen = availableMiraculous.size() - getChosenCount();

        float newTarget = unchosen == 0
                ? 0f
                : (float) ((Math.PI * 2) / unchosen);

        if (newTarget != targetAngleStep) {
            targetAngleStep = newTarget;
            targetWheelRotationAngle = -selectedOptionIndex * targetAngleStep;
        }

        // Smooth interpolation
        oldAngleStep = angleStep;
        angleStep += (targetAngleStep - angleStep) * 0.25f;
    }

    private void updateWheelRotation() {
        oldWheelRotationAngle = wheelRotationAngle;
        float delta = targetWheelRotationAngle - wheelRotationAngle;
        wheelRotationAngle += (delta * 0.25f);
    }

    private void createParticles() {
        final int NUMBER_OF_PARTICLES = 15;
        final int NUMBER_OF_CIRCLES = 25;
        for (int i = 0; i < NUMBER_OF_CIRCLES; i++) {
            double angleStep = Math.PI / (Math.random() * 10);
            int sign = Math.random() > 0.5 ? -1 : 1;
            double yOffset = Math.sin(Math.toRadians((double) 180 / Math.max(1, i) * sign));
            for (int j = 0; j < NUMBER_OF_PARTICLES * (i + 1); j++) {
                ParticleQuad newQuad = new ParticleQuad(Vec3.ZERO, yOffset, 0.1);
                newQuad.updateHorizontalPosition(i * 0.46, j * angleStep);
                particles.add(newQuad);
            }
        }
    }

    private record MiraculousOptionData(ItemStack stack, boolean chosen, float verticalOffset, float oldVerticalOffset) {
        public MiraculousOptionData withVerticalOffset(float verticalOffset) {
            return new MiraculousOptionData(this.stack, this.chosen, verticalOffset, this.verticalOffset);
        }
    }

    private static class ParticleQuad {
        private static final double OSCILLATION_SPEED = 0.02;
        private static final double OSCILLATION_AMPLITUDE = 0.8;

        private final double size;
        private final double yOffset;
        private Vec3 position;
        private Vec3 oldPosition;
        private boolean reversedOscillation;

        private ParticleQuad(Vec3 position, double yOffset, double size) {
            this.position = position;
            this.oldPosition = position;
            this.size = size;
            this.reversedOscillation = Math.random() > 0.5;
            this.yOffset = yOffset;
        }

        private void tick() {
            oldPosition = position;
            int sign = reversedOscillation ? -1 : 1;
            position = position.add(0, sign * OSCILLATION_SPEED, 0);
            if (Math.abs(position.y()) >= OSCILLATION_AMPLITUDE / 2) {
                reversedOscillation = !reversedOscillation;
            }
        }

        private void updateHorizontalPosition(double radius, double angle) {
            double x = -Math.cos(angle);
            double z = Math.sin(angle);
            this.position = new Vec3(x, position.y(), z).scale(radius);
        }

        private void render(MultiBufferSource multiBufferSource, PoseStack poseStack, float partialTick, int color) {
            poseStack.pushPose();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.text(PARTICLE_TEXTURE));
            vertexConsumer.setColor(255, 0, 0, 255);
            Vec3 position = oldPosition.lerp(this.position, partialTick);
            MineraculousClientUtils.vertex(vertexConsumer, poseStack.last(), position.add(-size, yOffset, size), 0, 0, LightTexture.FULL_BRIGHT, color);
            MineraculousClientUtils.vertex(vertexConsumer, poseStack.last(), position.add(size, yOffset, size), 1, 0, LightTexture.FULL_BRIGHT, color);
            MineraculousClientUtils.vertex(vertexConsumer, poseStack.last(), position.add(size, yOffset, -size), 1, 1, LightTexture.FULL_BRIGHT, color);
            MineraculousClientUtils.vertex(vertexConsumer, poseStack.last(), position.add(-size, yOffset, -size), 0, 1, LightTexture.FULL_BRIGHT, color);
            poseStack.popPose();
        }
    }
}
