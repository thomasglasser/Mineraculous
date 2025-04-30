package dev.thomasglasser.mineraculous.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class RadialMenuScreen<T extends RadialMenuOption> extends Screen {
    private static final float MAX_CIRCLE_SIZE = 180f;
    private static final float PRECISION = 2.5f / 360.0f;

    protected final List<T> options;
    protected final Consumer<T> onSelected;
    protected final int heldKey;
    protected final int selectedColor;

    private final double sliceAngle;

    protected double currentMouseX;
    protected double currentMouseY;
    protected int animationTick = 0;

    private float animationTime = 0;

    public RadialMenuScreen(List<T> options, Consumer<T> onSelected, int heldKey, int selectedColor) {
        super(Component.empty());
        this.options = options;
        this.onSelected = onSelected;
        this.heldKey = heldKey;
        this.selectedColor = selectedColor;
        this.sliceAngle = 2 * Math.PI / options.size();
    }

    private double alpha(int x, int y) {
        double alpha = Math.asin(Math.abs(y) / Math.sqrt(y * y + x * x));
        if (x > 0 && y >= 0) { //1
            return alpha;
        } else if (x <= 0 && y > 0) { //2
            return Math.PI - alpha;
        } else if (x < 0) { //3
            return Math.PI + alpha;
        } else if (y < 0) { //4
            return Math.PI - alpha + Math.PI;
        } else {
            return -1;
        }
    }

    // TODO: Ensure this always returns the same value
    protected int getSelectedOption(int pMouseX, int pMouseY, float circleSize) {
        double hypotenuse = Math.sqrt(pMouseX * pMouseX + pMouseY * pMouseY);
        if (hypotenuse < (circleSize) / 3f /*|| hypotenuse > circleSize * 91 / 90*/) {
            return -1;
        }
        double alpha = alpha(pMouseX, pMouseY);
        if (alpha != -1) {
            return (int) (alpha / sliceAngle);
        } else {
            return -1;
        }
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
        if (keyCode == heldKey) {
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        float circleSize;

        if (this.animationTick < MineraculousClientConfig.get().animationSpeed.get()) {
            animationTime = this.animationTick;
        }
        circleSize = animationTime * MAX_CIRCLE_SIZE / (float) MineraculousClientConfig.get().animationSpeed.get();
        circleSize /= 2f;
        int selectedOption = this.getSelectedOption((int) (currentMouseX - (double) width / 2), (int) (-1 * (currentMouseY - (double) height / 2)), circleSize);
        if (selectedOption != -1) {
            onSelected.accept(options.get(selectedOption));
        }
        super.onClose();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
    }

    @Override
    public void tick() {
        super.tick();
        if (animationTick < MineraculousClientConfig.get().animationSpeed.get()) {
            animationTick++;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int height = pGuiGraphics.guiHeight();
        int width = pGuiGraphics.guiWidth();
        int mouseX = pMouseX - width / 2;
        int mouseY = -1 * (pMouseY - height / 2);

        float circleSize;

        if (this.animationTick < MineraculousClientConfig.get().animationSpeed.get()) {
            animationTime = this.animationTick + pPartialTick;
        }
        circleSize = animationTime * MAX_CIRCLE_SIZE / (float) MineraculousClientConfig.get().animationSpeed.get();
        circleSize /= 2f;

        int selectedOption = getSelectedOption(mouseX, mouseY, circleSize);
        boolean hasSelectedOption = selectedOption != -1;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawPieArc(builder, width / 2f, height / 2f, 1, (circleSize) / 3f, circleSize * 91 / 90, (selectedOption + 1) * -sliceAngle, 2 * Math.PI - (selectedOption + 1) * sliceAngle, 0xAFAFAF);
        if (hasSelectedOption) {
            drawPieArc(builder, width / 2f, height / 2f, 0, (circleSize) / 3f, circleSize * 91 / 90, 2 * Math.PI - (selectedOption + 1) * sliceAngle, 2 * Math.PI - selectedOption * sliceAngle, selectedColor);
        }
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();

        for (int i = 0; i < options.size(); i++) {
            RadialMenuOption option = options.get(i);

            double angle = (i + 0.5) * sliceAngle;
            float radius = (circleSize) / 1.5f;

            float textX = width / 2f + (float) (radius * Math.cos(angle));
            float textY = height / 2f - (float) (radius * Math.sin(angle));

            pGuiGraphics.drawCenteredString(font, Component.translatable(option.translationKey()), (int) textX, (int) textY, 0xFFFFFF);
        }
    }

    protected void drawPieArc(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut, double startAngle, double endAngle, int color) {
        double angle = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(angle / PRECISION));

        angle = endAngle - startAngle;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        int a = 150;

        double slice = angle / sections;

        for (int i = 0; i < sections; i++) {
            double angle1 = startAngle + i * slice;
            double angle2 = startAngle + (i + 1) * slice;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.addVertex(pos1OutX, pos1OutY, z).setColor(r, g, b, a);
            buffer.addVertex(pos1InX, pos1InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2InX, pos2InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2OutX, pos2OutY, z).setColor(r, g, b, a);
        }
        ;
    }
}
