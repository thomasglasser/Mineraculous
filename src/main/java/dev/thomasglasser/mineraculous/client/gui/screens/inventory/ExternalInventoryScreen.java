package dev.thomasglasser.mineraculous.client.gui.screens.inventory;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class ExternalInventoryScreen extends InventoryScreen {
    public static final String ITEM_BOUND_KEY = "mineraculous.item_bound";

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
        guiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, xMouse, yMouse, target);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot.hasItem() && mouseButton == 0) {
            if (type == ClickType.PICKUP) {
                pickupHandler.handle(slot, target, menu);
                onClose(false);
            }
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
        void handle(Slot slot, Player target, AbstractContainerMenu menu);
    }

    @FunctionalInterface
    public interface CloseHandler {
        void onClose(boolean exit);
    }
}
