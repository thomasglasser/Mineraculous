package dev.thomasglasser.mineraculous.client.gui.screens.inventory;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;

public class ExternalInventoryScreen extends InventoryScreen {
    public static final String ITEM_BOUND_KEY = "mineraculous.item_bound";

    public static final ResourceLocation EXTERNAL_INVENTORY_LOCATION = Mineraculous.modLoc("textures/gui/container/external_inventory.png");

    protected final Player target;
    protected final boolean requireLooking;
    protected final ItemPickupHandler pickupHandler;
    protected final CloseHandler closeHandler;

    public ExternalInventoryScreen(Player target, boolean requireLooking, ItemPickupHandler pickupHandler, CloseHandler closeHandler) {
        super(target);
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
        if (slot.hasItem() && type == ClickType.PICKUP && pickupHandler.canPickUp(slot, target, menu)) {
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

    @FunctionalInterface
    public interface ItemPickupHandler {
        default boolean canPickUp(Slot slot, Player target, AbstractContainerMenu menu) {
            return slot.hasItem();
        }

        void pickUp(Slot slot, Player target, AbstractContainerMenu menu);
    }

    @FunctionalInterface
    public interface CloseHandler {
        void onClose(boolean exit);
    }
}
