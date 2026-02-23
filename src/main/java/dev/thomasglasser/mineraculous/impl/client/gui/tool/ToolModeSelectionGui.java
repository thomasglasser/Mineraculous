package dev.thomasglasser.mineraculous.impl.client.gui.tool;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.client.gui.components.selection.SelectionGui;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.client.gui.selection.categories.SelectionPage;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetMiraculousToolModePayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToolModeSelectionGui extends SelectionGui {
    private final int optionCount;

    public ToolModeSelectionGui(Minecraft minecraft, ToolModeMenuCategory category) {
        super(minecraft, g -> new SelectionMenu(g, category) {
            {
                if (!category.getItems().isEmpty()) {
                    this.selectedSlot = category.getItems().size() / 2;
                }
            }

            @Override
            public void selectSlot(int slot) {
                super.selectSlot(Math.min(slot, category.getItems().size() - 1));
            }
        });
        this.optionCount = category.getItems().size();
    }

    @Override
    public void onMouseScrolled(int amount) {
        if (this.isMenuActive()) {
            int i = this.menu.getSelectedSlot() + amount;
            if (i == -1) {
                i = optionCount - 1;
            } else if (i == optionCount) {
                i = 0;
            }
            this.menu.selectSlot(i);
        }
    }

    @Override
    public void onMenuClosed(SelectionMenu menu) {
        SelectionMenuItem selectedItem = menu.getSelectedItem();
        Player player = Minecraft.getInstance().player;
        if (selectedItem instanceof ToolModeMenuItem toolModeMenuItem) {
            ItemStack toolStack = player.getInventory().getSelected();
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousToolModePayload(toolStack, toolModeMenuItem.getToolMode().getSerializedName()));
            if (toolStack.getItem() instanceof ModeTool tool) {
                tool.setToolMode(toolStack, toolModeMenuItem.getToolMode());
            }
        }
        super.onMenuClosed(menu);
    }

    public void closeGui() {
        if (this.menu != null) {
            this.menu.exit();
        }
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

    @Override
    protected void renderPage(GuiGraphics guiGraphics, float alpha, int x, int y, SelectionPage selectionPage) {
        RenderSystem.enableBlend();
        guiGraphics.setColor(1, 1, 1, alpha);
        guiGraphics.pose().pushPose();
        int slot = Minecraft.getInstance().player.getInventory().selected;
        guiGraphics.pose().translate(Math.clamp((slot - (optionCount - 1) / 2), 0, 9 - optionCount) * 20f, 0, 0);
        for (int i = 0; i < optionCount; i++) {
            if (i == 0) {
                guiGraphics.blitSprite(SpectatorGui.HOTBAR_SPRITE, 182, 22, 0, 0, x - 91, y, 21, 22);
            } else if (i == optionCount - 1) {
                guiGraphics.blitSprite(SpectatorGui.HOTBAR_SPRITE, 182, 22, 161, 0, x - 91 + 20 * (optionCount - 1) + 1, y, 21, 22);
            } else {
                guiGraphics.blitSprite(SpectatorGui.HOTBAR_SPRITE, 182, 22, 20 * i + 1, 0, x - 91 + 20 * i + 1, y, 20, 22);
            }
        }
        if (selectionPage.getSelectedSlot() >= 0) {
            guiGraphics.blitSprite(SpectatorGui.HOTBAR_SELECTION_SPRITE, x - 91 - 1 + selectionPage.getSelectedSlot() * 20, y - 1, 24, 23);
        }

        guiGraphics.setColor(1, 1, 1, 1);
        Player player = Minecraft.getInstance().player;
        for (int i = 0; i < optionCount; i++) {
            this.renderSlot(guiGraphics, i, guiGraphics.guiWidth() / 2 - 90 + i * 20, y + 1f, alpha, selectionPage.getItem(i));
            if (player != null && selectionPage.getSelectedSlot() == i && selectionPage.getItem(i) instanceof ToolModeMenuItem toolModeMenuItem) {
                ItemStack stack = player.getInventory().getSelected();
                if (stack.getItem() instanceof ModeTool) {
                    Component component = toolModeMenuItem.getToolMode().displayName();
                    int xOffset = Minecraft.getInstance().font.width(component) / 2;
                    int xString = guiGraphics.guiWidth() / 2 - 90 + optionCount * 10 - xOffset;
                    guiGraphics.pose().pushPose();
                    guiGraphics.drawStringWithBackdrop(Minecraft.getInstance().font, component, xString, y - 12, 0, 0xFFF5F5F5);
                    guiGraphics.pose().popPose();
                }
            }
        }
        guiGraphics.pose().popPose();

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, int slot, int x, float y, float alpha, SelectionMenuItem selectionMenuItem) {
        if (selectionMenuItem != SelectionMenu.EMPTY_SLOT) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float) x, y, 0);
            float shadeColor = selectionMenuItem.isEnabled() ? 1 : 0.25f;
            guiGraphics.setColor(shadeColor, shadeColor, shadeColor, alpha);
            selectionMenuItem.renderIcon(guiGraphics, alpha);
            guiGraphics.setColor(1, 1, 1, 1);
            guiGraphics.pose().popPose();
        }
    }
}
