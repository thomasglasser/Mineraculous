package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.client.gui.components.selection.SelectionGui;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import java.util.function.Function;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ToolModeSelectionGui extends SelectionGui {
    public ToolModeSelectionGui(Minecraft minecraft, Function<SelectionGui, SelectionMenu> menuFunction) {
        super(minecraft, menuFunction);
    }

    @Override
    protected float getHotbarAlpha() {
        return 1;
    }

    @Override
    public void renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!isMenuActive()) {
            this.onMouseMiddleClick(); // force menu creation
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, -21, 0);
        super.renderHotbar(guiGraphics, deltaTracker);
        poseStack.popPose();
    }
}
