package dev.thomasglasser.mineraculous.api.client.gui.screens.inventory;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.ExternalMenuScreen;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
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

/**
 * Shows the inventory of another player,
 * performing logic provided by the pickup and close handlers when triggered.
 */
public abstract class ExternalInventoryScreen extends InventoryScreen implements ExternalMenuScreen {
    public static final Component ITEM_BOUND_KEY = Component.translatable("mineraculous.item_bound");

    public static final ResourceLocation EXTERNAL_INVENTORY_LOCATION = MineraculousConstants.modLoc("textures/gui/container/external_inventory.png");

    protected final Player target;
    protected final boolean requireLooking;

    public ExternalInventoryScreen(Player target, boolean requireLooking) {
        super(target);
        this.target = target;
        this.requireLooking = requireLooking;
    }

    @Override
    public void containerTick() {
        if (requireLooking && MineraculousClientUtils.getLookEntity() != target) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(EXTERNAL_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, xMouse, yMouse, target);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (!(slot.container instanceof CraftingContainer || slot.container instanceof ResultContainer)) {
            if (!canPickUp(slot, target, menu))
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
        if (canPickUp(slot, target, menu))
            super.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot.hasItem() && type == ClickType.PICKUP && canPickUp(slot, target, menu)) {
            pickUp(slot, target, menu);
            doOnClose(false);
        }
    }

    public final void doOnClose(boolean cancel) {
        super.onClose();
        onClose(cancel);
    }

    @Override
    public final void onClose() {
        doOnClose(true);
    }
}
