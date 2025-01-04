package dev.thomasglasser.mineraculous.client.gui.screens;

import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetToggleTagPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class KamikotizationChatScreen extends ChatScreen {
    public static final String INTRO_NAME = "gui.kamikotization.chat.intro.name";
    public static final String INTRO_NAMELESS = "gui.kamikotization.chat.intro.nameless";
    public static final String ACCEPT = "gui.kamikotization.chat.accept";

    private final boolean isButterfly;
    private final Player other;
    private final KamikotizationData kamikotizationData;

    protected Button acceptButton;

    public KamikotizationChatScreen(Player other, KamikotizationData kamikotizationData) {
        super("");
        this.isButterfly = false;
        this.other = other;
        this.kamikotizationData = kamikotizationData;
    }

    public KamikotizationChatScreen(String targetName, String butterflyName, Player other) {
        super(butterflyName.isEmpty() ? Component.translatable(INTRO_NAMELESS, targetName).getString() : Component.translatable(INTRO_NAME, targetName, butterflyName).getString());
        this.isButterfly = true;
        this.other = other;
        this.kamikotizationData = null;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.gui.getChat().clearMessages(true);
        this.commandSuggestions = null;
        if (!isButterfly) {
            this.acceptButton = Button.builder(Component.translatable(ACCEPT), button -> onClose(false, true))
                    .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
                    .build();
            this.addRenderableWidget(this.acceptButton);
        }
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
            if (isButterfly)
                onClose(true, true);
            else {
                tryCancel();
            }
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
        GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double) mouseX, (double) mouseY);
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

    public void tryCancel() {
        if (MineraculousServerConfig.INSTANCE.enableKamikotizationRejection.get())
            onClose(true, true);
    }

    public void onClose(boolean cancel, boolean initiated) {
        super.onClose();
        this.minecraft.gui.getChat().clearMessages(true);
        if (cancel) {
            if (this.isButterfly)
                TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(ClientUtils.getMainClientPlayer().getUUID(), other.blockPosition().above()));
            if (initiated)
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other.getUUID(), true));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
        } else {
            if (!this.isButterfly) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(kamikotizationData, true, false, false, ClientUtils.getMainClientPlayer().position().add(0, 1, 0)));
                TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(other.getUUID(), ClientUtils.getMainClientPlayer().getUUID(), kamikotizationData.kamikotization().orElseThrow()));
            }
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other.getUUID(), false));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
        }
    }
}
