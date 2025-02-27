package dev.thomasglasser.mineraculous.client.gui.screens;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ServerboundTransferMiraculousPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class MiraculousEligiblePlayerEntry extends ContainerObjectSelectionList.Entry<MiraculousEligiblePlayerEntry> {
    public static final Component RENOUNCE = Component.translatable("gui.miraculous_transfer.renounce");
    public static final Component TRANSFER = Component.translatable("gui.miraculous_transfer.transfer");
    private static final WidgetSprites RENOUNCE_BUTTON_SPRITES = new WidgetSprites(
            Mineraculous.modLoc("miraculous_transfer/renounce_button"),
            Mineraculous.modLoc("miraculous_transfer/renounce_button_highlighted"));
    private static final WidgetSprites TRANSFER_BUTTON_SPRITES = new WidgetSprites(
            Mineraculous.modLoc("miraculous_transfer/transfer_button"),
            Mineraculous.modLoc("miraculous_transfer/transfer_button_highlighted"));
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<PlayerSkin> skinGetter;
    @Nullable
    private Button transferButton;
    private float tooltipHoverTime;

    public MiraculousEligiblePlayerEntry(
            Minecraft minecraft, UUID id, String playerName, Supplier<PlayerSkin> skinGetter, int kwamiId) {
        this.minecraft = minecraft;
        this.id = id;
        this.playerName = playerName;
        this.skinGetter = skinGetter;
        if (minecraft.player.getUUID().equals(id)) {
            this.transferButton = new ImageButton(
                    0,
                    0,
                    20,
                    20,
                    RENOUNCE_BUTTON_SPRITES,
                    button -> {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundTransferMiraculousPayload(Optional.empty(), kwamiId));
                        minecraft.screen.onClose();
                    },
                    RENOUNCE) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return Component.literal(MiraculousEligiblePlayerEntry.this.playerName).append(", ").append(super.createNarrationMessage());
                }
            };
            this.transferButton.setTooltip(Tooltip.create(RENOUNCE));
        } else {
            this.transferButton = new ImageButton(
                    0,
                    0,
                    20,
                    20,
                    TRANSFER_BUTTON_SPRITES,
                    button -> {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundTransferMiraculousPayload(Optional.of(id), kwamiId));
                        minecraft.screen.onClose();
                    },
                    TRANSFER) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return Component.literal(MiraculousEligiblePlayerEntry.this.playerName).append(", ").append(super.createNarrationMessage());
                }
            };
            this.transferButton.setTooltip(Tooltip.create(TRANSFER));
        }
        this.transferButton.setTooltipDelay(PlayerEntry.TOOLTIP_DELAY);
        this.children = new ArrayList<>();
        this.children.add(this.transferButton);
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovering,
            float partialTick) {
        int i = left + 4;
        int j = top + (height - 24) / 2;
        int k = i + 24 + 4;
        int l;
        guiGraphics.fill(left, top, left + width, top + height, PlayerEntry.BG_FILL);
        l = top + (height - 9) / 2;
        PlayerFaceRenderer.draw(guiGraphics, this.skinGetter.get(), i, j, 24);
        guiGraphics.drawString(this.minecraft.font, this.playerName, k, l, PlayerEntry.PLAYERNAME_COLOR, false);
        if (this.transferButton != null) {
            float f = this.tooltipHoverTime;
            this.transferButton.setX(left + (width - this.transferButton.getWidth() - 4));
            this.transferButton.setY(top + (height - this.transferButton.getHeight()) / 2);
            this.transferButton.render(guiGraphics, mouseX, mouseY, partialTick);
            if (f == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0F;
            }
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerId() {
        return this.id;
    }
}
