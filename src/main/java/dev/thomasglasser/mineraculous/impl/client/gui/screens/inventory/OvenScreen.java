package dev.thomasglasser.mineraculous.impl.client.gui.screens.inventory;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.recipebook.OvenRecipeBookComponent;
import dev.thomasglasser.mineraculous.impl.world.inventory.OvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class OvenScreen extends AbstractFurnaceScreen<OvenMenu> {
    private static final ResourceLocation TEXTURE = MineraculousConstants.modLoc("textures/gui/container/oven.png");
    private static final ResourceLocation BURN_PROGRESS_SPRITE = MineraculousConstants.modLoc("container/oven/burn_progress");

    public OvenScreen(OvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, new OvenRecipeBookComponent(), playerInventory, title, TEXTURE, SmokerScreen.LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE);
    }

    @Override
    public void init() {
        super.init();
        clearWidgets();
        this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 38, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            button.setPosition(this.leftPos + 20, this.height / 2 - 38);
        }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int leftPos = this.leftPos;
        int topPos = this.topPos;
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int textureSize = 14;
            int progressPos = Mth.ceil(this.menu.getLitProgress() * textureSize - 1) + 1;
            guiGraphics.blitSprite(SmokerScreen.LIT_PROGRESS_SPRITE, textureSize, textureSize, 0, textureSize - progressPos, leftPos + 58, topPos + 38 + textureSize - progressPos, textureSize, progressPos);
        }

        float progress = this.menu.getBurnProgress();

        int horizontalWidth = 20;
        int verticalHeight = 14;

        int totalPathLength = horizontalWidth + verticalHeight;

        int currentFrame = (int) Math.ceil(totalPathLength * progress);

        int xWidth = Math.min(currentFrame, horizontalWidth);
        int xHeight = 5;
        if (xWidth > 0) {
            guiGraphics.blitSprite(BURN_PROGRESS_SPRITE, 26, 19, 0, 0, leftPos + 128, topPos + 17, xWidth, xHeight);
        }

        int verticalProgress = currentFrame - horizontalWidth;
        if (verticalProgress > 0) {
            int v_height = Math.min(verticalProgress, verticalHeight);
            guiGraphics.blitSprite(BURN_PROGRESS_SPRITE, 26, 19, 0, xHeight, leftPos + 128, topPos + 17 + xHeight, 26, v_height);
        }
    }
}
