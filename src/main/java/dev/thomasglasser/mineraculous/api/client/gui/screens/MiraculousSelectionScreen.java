package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MiraculousSelectionScreen extends Screen {
    private static final ResourceLocation CIRCLE_TEXTURE = MineraculousConstants.modLoc("textures/gui/sprites/miraculous_selection/circle.png");
    private static final float MAX_CIRCLE_RADIUS = 90;
    private static final float ITEM_SCALE_RELATIVE_TO_RADIUS = 0.05f;
    private static final float CIRCLE_RADIUS_PIXELS = 19;
    private static final int ITEM_MODEL_CENTER_OFFSET = -8;

    protected final int activationKey;

    private List<ItemStack> availableMiraculous = new ArrayList<>();
    private float oldCircleRadius = 0;
    private float circleRadius = 0;

    public MiraculousSelectionScreen(int activationKey) {
        super(Component.empty());
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
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /*@Override
    public void onClose() {
        int selectedOption = this.getSelectedOption((int) currentMouseX, (int) currentMouseY);
        if (selectedOption != -1) {
            onSelected.accept(options.get(selectedOption), selectedOption);
        }
        circleSize = 0f;
        tickCircleSize = 0f;
        oldTickCircleSize = 0f;
        super.onClose();
    }*/

    /*@Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        currentMouseX = mouseX - (double) width / 2;
        currentMouseY = -1 * (mouseY - (double) height / 2);
    }*/

    @Override
    public void tick() {
        super.tick();
        updateAvailableMiraculous();
        if (availableMiraculous.isEmpty()) {
            this.onClose();
        }
        oldCircleRadius = circleRadius;
        if (circleRadius < MAX_CIRCLE_RADIUS) {
            circleRadius++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        MultiBufferSource bufferSource = guiGraphics.bufferSource();
        int height = guiGraphics.guiHeight();
        int width = guiGraphics.guiWidth();
        mouseX = mouseX - width / 2;
        mouseY = -1 * (mouseY - height / 2);

        /*RenderSystem.backupProjectionMatrix();

        Matrix4f perspective = new Matrix4f()
                .setPerspective(
                        (float) Math.toRadians(45.0f),
                        (float) width / height,
                        0.0001f,
                        2000
                );

        RenderSystem.setProjectionMatrix(perspective, VertexSorting.DISTANCE_TO_ORIGIN);
*/
        /*
        poseStack.pushPose();
        poseStack.translate(width / 2d, height / 2d, 0);
        float wheelSize = getCircleRadius(partialTick);
        poseStack.scale(wheelSize, wheelSize, wheelSize);
        //poseStack.rotateAround(new Quaternionf().rotateTo(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0.7f)), 0, 0, 0);
        //poseStack.mulPose(Axis.XP.rotationDegrees(80f));
        //poseStack.mulPose(Axis.XP.rotationDegrees(80f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(mouseX));
        renderMiraculous(guiGraphics, poseStack);
        renderGoldenCircle(poseStack, bufferSource);

        poseStack.popPose();*/
    }

    private float getCircleRadius(float partialTick) {
        return Mth.lerp(partialTick, oldCircleRadius, circleRadius);
    }

    private static void renderGoldenCircle(PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.text(CIRCLE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(-1, 1, 0), 0, 0, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(1, 1, 0), 1, 0, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(1, -1, 0), 1, 1, LightTexture.FULL_BRIGHT);
        MineraculousClientUtils.vertex(vertexConsumer, pose, new Vec3(-1, -1, 0), 0, 1, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private void renderMiraculous(GuiGraphics guiGraphics, PoseStack poseStack) {
        if (!availableMiraculous.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(ITEM_SCALE_RELATIVE_TO_RADIUS, ITEM_SCALE_RELATIVE_TO_RADIUS, ITEM_SCALE_RELATIVE_TO_RADIUS);
            int numberOfOptions = availableMiraculous.size() + 1;
            double angleStep = 2 * Math.PI / numberOfOptions;

            for (int i = 0; i < numberOfOptions; i++) {
                double angle = i * angleStep;
                double x = CIRCLE_RADIUS_PIXELS * Math.cos(angle);
                double y = CIRCLE_RADIUS_PIXELS * Math.sin(angle);
                boolean lastOption = i == numberOfOptions - 1; // last option is the cancel button
                if (!lastOption) {
                    ItemStack miraculous = availableMiraculous.get(i);
                    poseStack.pushPose();
                    Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0));
                    //poseStack.mulPose(Axis.XP.rotationDegrees(-80f));
                    poseStack.translate(x, y, 0);
                    poseStack.rotateAround(rotation, (float) x, (float) y, 0);
                    guiGraphics.renderItem(miraculous, ITEM_MODEL_CENTER_OFFSET, ITEM_MODEL_CENTER_OFFSET);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }
    }

    private void updateAvailableMiraculous() {
        List<ItemStack> miraculouses = new ArrayList<>();
        for (ItemStack stack : CuriosUtils.getAllItems(ClientUtils.getLocalPlayer()).values()) {
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                ResourceKey<Miraculous> key = miraculous.getKey();
                if (key != null) {
                    ItemStack chargedCopy = stack.copy();
                    chargedCopy.set(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
                    miraculouses.add(chargedCopy);
                }
            }
        }
        availableMiraculous.clear();
        availableMiraculous.addAll(miraculouses);
    }
}
