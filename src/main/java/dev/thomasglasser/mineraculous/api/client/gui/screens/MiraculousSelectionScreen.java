package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class MiraculousSelectionScreen extends Screen {
    protected final int activationKey;
    private List<Holder<Miraculous>> availableMiraculous;

    protected MiraculousSelectionScreen(int activationKey, List<Holder<Miraculous>> availableMiraculous) {
        super(Component.empty());
        this.activationKey = activationKey;
        this.availableMiraculous = availableMiraculous;
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
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int height = pGuiGraphics.guiHeight();
        int width = pGuiGraphics.guiWidth();
        int mouseX = pMouseX - width / 2;
        int mouseY = -1 * (pMouseY - height / 2);
    }
}
