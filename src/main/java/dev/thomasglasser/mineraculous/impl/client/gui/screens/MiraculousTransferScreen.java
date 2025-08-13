package dev.thomasglasser.mineraculous.impl.client.gui.screens;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MiraculousTransferScreen extends Screen {
    public static final Component TITLE = Component.translatable("screen.miraculous_transfer.title");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final int kwamiId;
    private MiraculousEligiblePlayerList playerList;
    private EditBox searchBox;
    private String lastSearch = "";

    public MiraculousTransferScreen(int kwamiId) {
        super(TITLE);
        this.kwamiId = kwamiId;
    }

    private int windowHeight() {
        return Math.max(52, this.height - 128 - SocialInteractionsScreen.SEARCH_HEIGHT);
    }

    private int listEnd() {
        return 80 + this.windowHeight() - SocialInteractionsScreen.BG_BORDER_SIZE;
    }

    private int marginX() {
        return (this.width - SocialInteractionsScreen.IMAGE_WIDTH) / 2;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.playerList = new MiraculousEligiblePlayerList(this.minecraft, this.width, this.listEnd() - SocialInteractionsScreen.LIST_START, SocialInteractionsScreen.LIST_START, SocialInteractionsScreen.ITEM_HEIGHT, this.kwamiId);
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, this.marginX() + 28, 74, 200, 15, SocialInteractionsScreen.SEARCH_HINT) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return !searchBox.getValue().isEmpty() && playerList.isEmpty()
                        ? super.createNarrationMessage().append(", ").append(SocialInteractionsScreen.EMPTY_SEARCH)
                        : super.createNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(-1);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SocialInteractionsScreen.SEARCH_HINT);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.addRenderableWidget(this.searchBox);
        this.addWidget(playerList);
        this.playerList.updatePlayerList(this.playerList.getScrollAmount());

        GameNarrator gamenarrator = this.minecraft.getNarrator();
        if (!this.searchBox.getValue().isEmpty() && this.playerList.isEmpty() && !this.searchBox.isFocused()) {
            gamenarrator.sayNow(SocialInteractionsScreen.EMPTY_SEARCH);
        }
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).width(200).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.playerList.updateSizeAndPosition(this.width, this.listEnd() - SocialInteractionsScreen.LIST_START, SocialInteractionsScreen.LIST_START);
        this.searchBox.setPosition(this.marginX() + 28, 74);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.marginX() + 3;
        guiGraphics.blitSprite(SocialInteractionsScreen.BACKGROUND_SPRITE, i, SocialInteractionsScreen.MARGIN_Y, SocialInteractionsScreen.BG_WIDTH, this.windowHeight() + SocialInteractionsScreen.SEARCH_HEIGHT);
        guiGraphics.blitSprite(SocialInteractionsScreen.SEARCH_SPRITE, i + 10, 76, 12, 12);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!this.playerList.isEmpty()) {
            this.playerList.render(guiGraphics, mouseX, mouseY, partialTick);
        } else if (!this.searchBox.getValue().isEmpty()) {
            guiGraphics.drawCenteredString(this.minecraft.font, SocialInteractionsScreen.EMPTY_SEARCH, this.width / 2, (SocialInteractionsScreen.SEARCH_START + this.listEnd()) / 2, -1);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void checkSearchStringUpdate(String newText) {
        newText = newText.toLowerCase();
        if (!newText.equals(this.lastSearch)) {
            this.playerList.setFilter(newText);
            this.lastSearch = newText;
        }
    }
}
