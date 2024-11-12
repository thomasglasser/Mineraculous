package dev.thomasglasser.mineraculous.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class ExternalCuriosInventoryScreen extends CuriosScreen {
    static final ResourceLocation CURIO_INVENTORY = ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID,
            "textures/gui/curios/inventory.png");

    private static int scrollCooldown = 0;

    protected final Player target;
    protected final boolean requireLooking;
    protected final ExternalInventoryScreen.ItemPickupHandler pickupHandler;
    protected final ExternalInventoryScreen.CloseHandler closeHandler;

    public ExternalCuriosInventoryScreen(Player target, boolean requireLooking, ExternalInventoryScreen.ItemPickupHandler pickupHandler, ExternalInventoryScreen.CloseHandler closeHandler) {
        super(new CuriosContainer(-1, target.getInventory()), target.getInventory(), Component.translatable("container.crafting"));
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX,
            int mouseY) {
        if (this.minecraft != null && target != null) {

            if (scrollCooldown > 0 && target.tickCount % 5 == 0) {
                scrollCooldown--;
            }
            this.panelWidth = this.menu.panelWidth;
            int i = this.leftPos;
            int j = this.topPos;
            guiGraphics.blit(RenderType::guiTextured, INVENTORY_LOCATION, i, j, 0, 0, 176, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75,
                    j + 78, 30, 0.0625F, mouseX, mouseY, target);
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                int xOffset = -33;
                int yOffset = j;
                boolean pageOffset = this.menu.totalPages > 1;

                if (this.menu.hasCosmetics) {
                    guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset + 2, yOffset - 23, 32, 0, 28, 24, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
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
                    guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset, yOffset, xTexOffset, 0, 25, upperHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
                    guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset, yOffset + upperHeight, xTexOffset, 159, 25, 7, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

                    if (grid.size() == 1) {
                        xTexOffset += 7;
                        guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset + 7, yOffset, xTexOffset, 0, 25, upperHeight, 25, upperHeight);
                        guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset + 7, yOffset + upperHeight, xTexOffset, 159, 25, 7, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
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

                    guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, i + xOffset, yOffset + 7, 7, 7, 18, upperHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
                    xOffset += 18;
                }
                RenderSystem.enableBlend();

                for (Slot slot : this.menu.slots) {

                    if (slot instanceof CurioSlot curioSlot && curioSlot.isCosmetic()) {
                        guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, slot.x + this.getGuiLeft() - 1, slot.y + this.getGuiTop() - 1, 32, 50, 18, 18, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
                    }
                }
                RenderSystem.disableBlend();
            });
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton,
            ClickType type) {
        if (slot != null && slot.hasItem() && mouseButton == 0) {
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
}
