package dev.thomasglasser.mineraculous.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.gui.CosmeticButton;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.PageButton;
import top.theillusivec4.curios.client.gui.RenderButton;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class ExternalCuriosInventoryScreen extends CuriosScreen {
    public static final ResourceLocation CURIO_INVENTORY_LOCATION = ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID,
            "textures/gui/curios/inventory.png");

    private static int scrollCooldown = 0;

    protected final Player target;
    protected final boolean requireLooking;
    protected final ExternalInventoryScreen.ItemPickupHandler pickupHandler;
    protected final ExternalInventoryScreen.CloseHandler closeHandler;

    protected PageButton nextPage;
    protected PageButton prevPage;

    public ExternalCuriosInventoryScreen(Player target, boolean requireLooking, ExternalInventoryScreen.ItemPickupHandler pickupHandler, ExternalInventoryScreen.CloseHandler closeHandler) {
        super(new CuriosContainer(-1, target.getInventory()), target.getInventory(), Component.empty());
        this.target = target;
        this.requireLooking = requireLooking;
        this.pickupHandler = pickupHandler;
        this.closeHandler = closeHandler;
    }

    @Override
    public void containerTick() {
        if (requireLooking && MineraculousClientUtils.getLookEntity() != target) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public void init() {
        if (this.minecraft != null) {
            this.panelWidth = this.menu.panelWidth;
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.topPos = (this.height - this.imageHeight) / 2;
            this.widthTooNarrow = true;

            this.updateRenderButtons();
        }
    }

    @Override
    public void updateRenderButtons() {
        this.narratables.removeIf(
                widget -> widget instanceof RenderButton
                        || widget instanceof CosmeticButton
                        || widget instanceof PageButton);
        this.children.removeIf(
                widget -> widget instanceof RenderButton
                        || widget instanceof CosmeticButton
                        || widget instanceof PageButton);
        this.renderables.removeIf(
                widget -> widget instanceof RenderButton
                        || widget instanceof CosmeticButton
                        || widget instanceof PageButton);
        this.panelWidth = this.menu.panelWidth;

        if (this.menu.totalPages > 1) {
            this.nextPage = new PageButton(
                    this, this.getGuiLeft() + 17, this.getGuiTop() + 2, 11, 12, PageButton.Type.NEXT);
            this.addRenderableWidget(this.nextPage);
            this.prevPage = new PageButton(
                    this, this.getGuiLeft() + 17, this.getGuiTop() + 2, 11, 12, PageButton.Type.PREVIOUS);
            this.addRenderableWidget(this.prevPage);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this.minecraft != null && this.minecraft.player != null) {
            if (scrollCooldown > 0 && this.minecraft.player.tickCount % 5 == 0) {
                scrollCooldown--;
            }
            this.panelWidth = this.menu.panelWidth;
            int i = this.leftPos;
            int j = this.topPos;
            guiGraphics.blit(ExternalInventoryScreen.EXTERNAL_INVENTORY_LOCATION, i, j, 0, 0, 176, this.imageHeight);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    i + 26,
                    j + 8,
                    i + 75,
                    j + 78,
                    30,
                    0.0625F,
                    mouseX,
                    mouseY,
                    this.target);
            CuriosApi.getCuriosInventory(this.target)
                    .ifPresent(
                            handler -> {
                                int xOffset = -33;
                                int yOffset = j;
                                boolean pageOffset = this.menu.totalPages > 1;

                                if (this.menu.hasCosmetics) {
                                    guiGraphics.blit(CURIO_INVENTORY_LOCATION, i + xOffset + 2, yOffset - 23, 32, 0, 28, 24);
                                }
                                List<Integer> grid = this.menu.grid;
                                xOffset -= (grid.size() - 1) * 18;

                                // render backplate
                                for (int r = 0; r < grid.size(); r++) {
                                    int rows = grid.getFirst();
                                    int upperHeight = 7 + rows * 18;
                                    int xTexOffset = 91;

                                    if (pageOffset) {
                                        upperHeight += 8;
                                    }

                                    if (r != 0) {
                                        xTexOffset += 7;
                                    }
                                    guiGraphics.blit(
                                            CURIO_INVENTORY_LOCATION, i + xOffset, yOffset, xTexOffset, 0, 25, upperHeight);
                                    guiGraphics.blit(
                                            CURIO_INVENTORY_LOCATION, i + xOffset, yOffset + upperHeight, xTexOffset, 159, 25, 7);

                                    if (grid.size() == 1) {
                                        xTexOffset += 7;
                                        guiGraphics.blit(
                                                CURIO_INVENTORY_LOCATION, i + xOffset + 7, yOffset, xTexOffset, 0, 25, upperHeight);
                                        guiGraphics.blit(
                                                CURIO_INVENTORY_LOCATION,
                                                i + xOffset + 7,
                                                yOffset + upperHeight,
                                                xTexOffset,
                                                159,
                                                25,
                                                7);
                                    }

                                    if (r == 0) {
                                        xOffset += 25;
                                    } else {
                                        xOffset += 18;
                                    }
                                }
                                xOffset -= (grid.size()) * 18;

                                if (pageOffset) {
                                    yOffset += 8;
                                }

                                // render slots
                                for (int rows : grid) {
                                    int upperHeight = rows * 18;

                                    guiGraphics.blit(
                                            CURIO_INVENTORY_LOCATION, i + xOffset, yOffset + 7, 7, 7, 18, upperHeight);
                                    xOffset += 18;
                                }
                                RenderSystem.enableBlend();

                                for (Slot slot : this.menu.slots) {

                                    if (slot instanceof CurioSlot curioSlot && curioSlot.isCosmetic()) {
                                        guiGraphics.blit(
                                                CURIO_INVENTORY_LOCATION,
                                                slot.x + this.getGuiLeft() - 1,
                                                slot.y + this.getGuiTop() - 1,
                                                32,
                                                50,
                                                18,
                                                18);
                                    }
                                }
                                RenderSystem.disableBlend();
                            });
        }
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (!(slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer)) {
            if (!pickupHandler.canPickUp(slot, target, menu))
                renderDisabledSlot(guiGraphics, slot);
            else
                super.renderSlot(guiGraphics, slot);
        }
    }

    private void renderDisabledSlot(GuiGraphics guiGraphics, Slot slot) {
        guiGraphics.blitSprite(CrafterScreen.DISABLED_SLOT_LOCATION_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if (pickupHandler.canPickUp(slot, target, menu))
            super.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot != null && slot.hasItem() && type == ClickType.PICKUP && pickupHandler.canPickUp(slot, target, menu)) {
            pickupHandler.pickUp(slot, target, menu);
            onClose(false);
        }
    }

    public void onClose(boolean cancel) {
        super.onClose();
        closeHandler.onClose(cancel);
    }

    @Override
    public void onClose() {
        onClose(true);
    }
}
