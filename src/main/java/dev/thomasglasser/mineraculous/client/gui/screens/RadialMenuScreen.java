package dev.thomasglasser.mineraculous.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RadialMenuScreen extends Screen {
    private static final float MAX_CIRCLE_SIZE = 180f;
    private static final float PRECISION = 2.5f / 360.0f;
    private static final int MAX_ANIMATION_TICKS = 10;

    protected final List<RadialMenuOption> options;
    protected final ItemStack stack;
    protected final Consumer<RadialMenuOption> onSelected;
    protected final int heldKey;
    protected final int selectedColor;

    private final double sliceAngle;

    private float animationTime = 0;

    protected int animationTick = 0;

    public RadialMenuScreen(List<RadialMenuOption> options, ItemStack stack, Consumer<RadialMenuOption> onSelected, int heldKey, int selectedColor) {
        super(Component.empty());
        this.options = options;
        this.stack = stack;
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

    protected int getSelectedOption(int pMouseX, int pMouseY) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int selectedOption = this.getSelectedOption((int) (mouseX - (double) width / 2), (int) (-1 * (mouseY - (double) height / 2)));
            if (selectedOption != -1) {
                onSelected.accept(options.get(selectedOption));
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(ClientUtils.getMainClientPlayer());
                playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 20);
                TommyLibServices.ENTITY.setPersistentData(ClientUtils.getMainClientPlayer(), playerData, false);
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        super.tick();
        if (animationTick < MAX_ANIMATION_TICKS) {
            animationTick++;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int height = pGuiGraphics.guiHeight();
        int width = pGuiGraphics.guiWidth();
        int mouseX = pMouseX - width / 2;
        int mouseY = -1 * (pMouseY - height / 2);

        int selectedOption = getSelectedOption(mouseX, mouseY);
        boolean hasSelectedOption = selectedOption != -1;
        RadialMenuOption selected = hasSelectedOption ? options.get(selectedOption) : null;
        float circleSize;

        if (this.animationTick < MAX_ANIMATION_TICKS) {
            animationTime = this.animationTick + pPartialTick;
        }
        circleSize = animationTime * MAX_CIRCLE_SIZE / (float) MAX_ANIMATION_TICKS;
        circleSize /= 2f;

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
        if (hasSelectedOption) {
            int centerWidth = width / 2;
            int centerHeight = height / 2;
            pGuiGraphics.drawCenteredString(font, Component.translatable(selected.translationKey()), centerWidth, centerHeight - font.lineHeight / 2, 0xFFFFFF);
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
    }
}
