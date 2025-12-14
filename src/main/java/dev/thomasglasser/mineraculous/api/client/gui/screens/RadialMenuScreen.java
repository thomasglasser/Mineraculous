package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetRadialMenuProviderOptionPayload;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Displays a list of {@link RadialMenuOption}s and allows selection while the provided key is held.
 * 
 * @param <T> The type of {@link RadialMenuOption} to display.
 */
public class RadialMenuScreen<T extends RadialMenuOption> extends Screen {
    public static final float MAX_CIRCLE_SIZE = 100f;
    private static final float PRECISION = 2.5f / 360.0f;

    protected final int heldKey;
    protected final List<T> options;
    protected final int selectedColor;
    protected final BiConsumer<T, Integer> onSelected;

    private final double sliceAngle;

    protected double currentMouseX;
    protected double currentMouseY;

    private float circleSize = 0;
    private static float tickCircleSize = 0f;
    private static float oldTickCircleSize = 0f;

    public static float getInterpolatedRadialCircleSize(float partialTicks) {
        return Mth.lerp(partialTicks, oldTickCircleSize, tickCircleSize);
    }

    public RadialMenuScreen(int heldKey, List<T> options, int selectedColor, BiConsumer<T, Integer> onSelected) {
        super(Component.empty());
        this.heldKey = heldKey;
        this.options = options;
        this.selectedColor = selectedColor;
        this.onSelected = onSelected;
        this.sliceAngle = 2 * Math.PI / options.size();
    }

    public RadialMenuScreen(InteractionHand hand, int heldKey, ItemStack stack, RadialMenuProvider<T> provider) {
        this(heldKey, provider.getEnabledOptions(stack, hand, ClientUtils.getLocalPlayer()), provider.getColor(stack, hand, ClientUtils.getLocalPlayer()), (selected, index) -> {
            if (stack.get(provider.getComponentType(stack, hand, ClientUtils.getLocalPlayer())) != selected)
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetRadialMenuProviderOptionPayload(hand, index));
        });
    }

    private double alpha(int x, int y) {
        double alpha = Math.asin(Mth.abs(y) / Mth.sqrt(y * y + x * x));
        if (x >= 0 && y >= 0) { // Q1
            return alpha;
        } else if (x < 0 && y >= 0) { // Q2
            return Math.PI - alpha;
        } else if (x < 0) { // Q3
            return Math.PI + alpha;
        } else { // Q4
            return Math.PI - alpha + Math.PI;
        }
    }

    protected int getSelectedOption(int pMouseX, int pMouseY) {
        double hypotenuse = Mth.sqrt(pMouseX * pMouseX + pMouseY * pMouseY);
        // No selected option if in the middle of the circle
        if (hypotenuse < (circleSize) / 3f) {
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
        return true;
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == heldKey) {
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        int selectedOption = this.getSelectedOption((int) currentMouseX, (int) currentMouseY);
        if (selectedOption != -1) {
            onSelected.accept(options.get(selectedOption), selectedOption);
        }
        circleSize = 0f;
        tickCircleSize = 0f;
        oldTickCircleSize = 0f;
        super.onClose();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        currentMouseX = mouseX - (double) width / 2;
        currentMouseY = -1 * (mouseY - (double) height / 2);
    }

    @Override
    public void tick() {
        super.tick();
        oldTickCircleSize = tickCircleSize;
        tickCircleSize += MineraculousClientConfig.get().animationSpeed.get(); // adds the expected amount per tick
        tickCircleSize = Math.min(tickCircleSize, MAX_CIRCLE_SIZE);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int height = pGuiGraphics.guiHeight();
        int width = pGuiGraphics.guiWidth();
        int mouseX = pMouseX - width / 2;
        int mouseY = -1 * (pMouseY - height / 2);
        circleSize = getInterpolatedRadialCircleSize(pPartialTick);

        int selectedOption = getSelectedOption(mouseX, mouseY);
        boolean hasSelectedOption = selectedOption != -1;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawPieArc(builder, width / 2f, height / 2f, 1, (circleSize) / 3f, circleSize * 91 / 90, (selectedOption + 1) * -sliceAngle, 2 * Math.PI - (selectedOption + 1) * sliceAngle, 0xAFAFAF);
        if (hasSelectedOption) {
            int color = selectedColor;
            Integer override = options.get(selectedOption).colorOverride();
            if (override != null)
                color = override;
            drawPieArc(builder, width / 2f, height / 2f, 0, (circleSize) / 3f, circleSize * 91 / 90, 2 * Math.PI - (selectedOption + 1) * sliceAngle, 2 * Math.PI - selectedOption * sliceAngle, color);
        }
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();

        for (int i = 0; i < options.size(); i++) {
            RadialMenuOption option = options.get(i);

            float angle = (float) ((i + 0.5) * sliceAngle);
            float radius = (circleSize) / 1.5f;

            float textX = width / 2f + radius * Mth.cos(angle);
            float textY = height / 2f - radius * Mth.sin(angle);

            pGuiGraphics.drawCenteredString(font, option.displayName(), (int) textX, (int) textY, 0xFFFFFF);
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
            float angle1 = (float) (startAngle + i * slice);
            float angle2 = (float) (startAngle + (i + 1) * slice);

            float pos1InX = x + radiusIn * Mth.cos(angle1);
            float pos1InY = y + radiusIn * Mth.sin(angle1);
            float pos1OutX = x + radiusOut * Mth.cos(angle1);
            float pos1OutY = y + radiusOut * Mth.sin(angle1);
            float pos2OutX = x + radiusOut * Mth.cos(angle2);
            float pos2OutY = y + radiusOut * Mth.sin(angle2);
            float pos2InX = x + radiusIn * Mth.cos(angle2);
            float pos2InY = y + radiusIn * Mth.sin(angle2);

            buffer.addVertex(pos1OutX, pos1OutY, z).setColor(r, g, b, a);
            buffer.addVertex(pos1InX, pos1InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2InX, pos2InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2OutX, pos2OutY, z).setColor(r, g, b, a);
        }
    }
}
