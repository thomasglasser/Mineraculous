package dev.thomasglasser.mineraculous.impl.client.gui;

import dev.thomasglasser.mineraculous.api.client.gui.components.selection.SelectionGui;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.client.gui.kamiko.categories.KamikoTargetPlayerMenuCategory;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRevertConvertedEntityPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetSpectationInterruptedPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStartKamikotizationDetransformationPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class MineraculousGuis {
    public static final Component REVOKE = Component.translatable("gui.mineraculous.revoke");
    public static final Component REVOKE_WITH_SPACE = Component.translatable("gui.mineraculous.revoke_with_space");

    private static SelectionGui kamikoGui;
    private static Button revokeButton;

    public static SelectionGui getKamikoGui() {
        if (kamikoGui == null) {
            kamikoGui = new SelectionGui(Minecraft.getInstance(), gui -> new SelectionMenu(gui, new KamikoTargetPlayerMenuCategory()) {
                @Override
                public void selectSlot(int slot) {
                    SelectionMenuItem selectionMenuItem = this.getItem(slot);
                    if (selectionMenuItem != SelectionMenu.CLOSE_ITEM && Minecraft.getInstance().level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).size() == 0) {
                        Minecraft.getInstance().player.displayClientMessage(Kamikotization.NO_KAMIKOTIZATIONS, true);
                    } else {
                        super.selectSlot(slot);
                    }
                }
            });
        }
        return kamikoGui;
    }

    public static Button getRevokeButton() {
        if (revokeButton == null) {
            revokeButton = new Button(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35, 200, 20, REVOKE, button -> {
                Entity cameraEntity = MineraculousClientUtils.getCameraEntity();
                if (cameraEntity instanceof Player target) {
                    KamikotizationData kamikotizationData = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                    TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationDetransformationPayload(Optional.of(target.getUUID()), kamikotizationData, false));
                    AbilityEffectData.removeFaceMaskTexture(target, kamikotizationData.kamikoData().faceMaskTexture());
                } else if (cameraEntity instanceof Kamiko kamiko) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundRevertConvertedEntityPayload(kamiko.getOwner().getId(), kamiko.getId()));
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetSpectationInterruptedPayload(Optional.empty()));
            }, Button.DEFAULT_NARRATION) {
                @Override
                public Component getMessage() {
                    if (MineraculousClientUtils.hasNoScreenOpen())
                        return REVOKE_WITH_SPACE;
                    else
                        return REVOKE;
                }

                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    if (active) {
                        if (keyCode == GLFW.GLFW_KEY_SPACE) {
                            revokeButton.onPress();
                            return true;
                        }
                    }
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            };
        }
        return revokeButton;
    }

    public static void renderStealingProgressBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        int width = MineraculousKeyMappings.getTakeTicks();
        if (player != null && width > 0) {
            int x = (guiGraphics.guiWidth() - 18) / 2;
            int y = (guiGraphics.guiHeight() + 12) / 2;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
            guiGraphics.fill(RenderType.guiOverlay(), x, y, (int) (x + (width / 5.0)), y + 5, 0xFFFFFFF | -16777216);
        }
    }

    public static void renderRevokeButton(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Button revokeButton = getRevokeButton();
        if (MineraculousClientUtils.isInKamikoView() && !kamikoGui.isMenuActive()) {
            if (MineraculousClientUtils.hasNoScreenOpen()) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 60);
                revokeButton.active = true;
            } else if (Minecraft.getInstance().screen instanceof ChatScreen) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35);
                revokeButton.active = true;
            } else
                revokeButton.active = false;
        } else
            revokeButton.active = false;

        if (revokeButton.active) {
            int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth()
                    / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                    * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight()
                    / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            revokeButton.render(guiGraphics, mouseX, mouseY, 0);
        }
    }
}
