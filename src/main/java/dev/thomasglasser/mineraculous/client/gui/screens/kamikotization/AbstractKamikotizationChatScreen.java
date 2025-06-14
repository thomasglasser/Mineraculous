package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractKamikotizationChatScreen extends ChatScreen {
    protected final Optional<ResourceLocation> faceMaskTexture;

    protected AbstractKamikotizationChatScreen(String initialText, Optional<ResourceLocation> faceMaskTexture) {
        super(initialText);
        this.faceMaskTexture = faceMaskTexture;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.gui.getChat().clearMessages(true);
        this.commandSuggestions = null;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.input.getValue();
        ArrayList<GuiMessage> list = new ArrayList<>();
        for (int i = this.minecraft.gui.getChat().allMessages.size() - 1; i >= 0; i--) {
            list.add(this.minecraft.gui.getChat().allMessages.get(i));
        }
        this.init(minecraft, width, height);
        for (GuiMessage message : list) {
            this.minecraft.gui.getChat().addMessage(message.content());
        }
        this.setChatLine(s);
    }

    @Override
    protected void onEdited(String value) {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose(true, true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.handleChatInput(this.input.getValue(), true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
        } else {
            FocusNavigationEvent focusnavigationevent = switch (keyCode) {
                case 258 -> this.createTabEvent();
                case 262 -> this.createArrowEvent(ScreenDirection.RIGHT);
                case 263 -> this.createArrowEvent(ScreenDirection.LEFT);
                default -> null;
            };
            if (focusnavigationevent != null) {
                ComponentPath componentpath = super.nextFocusPath(focusnavigationevent);
                if (componentpath == null && focusnavigationevent instanceof FocusNavigationEvent.TabNavigation) {
                    this.clearFocus();
                    componentpath = super.nextFocusPath(focusnavigationevent);
                }

                if (componentpath != null) {
                    this.changeFocus(componentpath);
                }
            }

            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollY = Mth.clamp(scrollY, -1.0, 1.0);
        if (!hasShiftDown()) {
            scrollY *= 7.0;
        }

        this.minecraft.gui.getChat().scrollChat((int) scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            ChatComponent chatcomponent = this.minecraft.gui.getChat();
            if (chatcomponent.handleChatQueueClicked(mouseX, mouseY)) {
                return true;
            }

            Style style = this.getComponentStyleAt(mouseX, mouseY);
            if (style != null && this.handleComponentClicked(style)) {
                this.initial = this.input.getValue();
                return true;
            }
        }

        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return this.input.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.minecraft.gui.getChat().render(guiGraphics, this.minecraft.gui.getGuiTicks(), mouseX, mouseY, true);
        guiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(guiGraphics, mouseX, mouseY, partialTick);
        GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt(mouseX, mouseY);
        if (guimessagetag != null && guimessagetag.text() != null) {
            guiGraphics.renderTooltip(this.font, this.font.split(guimessagetag.text(), 210), mouseX, mouseY);
        } else {
            Style style = this.getComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getHoverEvent() != null) {
                guiGraphics.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level == null) {
            this.renderPanorama(guiGraphics, partialTick);
        }
        this.renderBlurredBackground(partialTick);
        this.renderMenuBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void handleChatInput(String message, boolean addToRecentChat) {
        message = this.normalizeChatMessage(message);
        if (!message.isEmpty()) {
            if (addToRecentChat) {
                this.minecraft.gui.getChat().addRecentChat(message);
            }

            this.minecraft.player.connection.sendChat(message);
            setChatLine("");
        }
    }

    protected final void finalizeClose() {
        super.onClose();
        this.minecraft.gui.getChat().clearMessages(true);
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            AbilityEffectData.removeFaceMaskTexture(player, faceMaskTexture);
        }
    }

    @Override
    public void onClose() {
        onClose(true, true);
    }

    public abstract void onClose(boolean cancel, boolean initiated);
}
