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
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class MineraculousGuis {
    public static final Component REVOKE = Component.translatable("gui.mineraculous.revoke");
    public static final String PRESS_KEY = "gui.mineraculous.press_key";

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
                if (cameraEntity instanceof Kamiko kamiko && kamiko.getOwner() != null) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundRevertConvertedEntityPayload(kamiko.getOwner().getId(), kamiko.getId()));
                } else if (cameraEntity instanceof LivingEntity target) {
                    KamikotizationData kamikotizationData = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                    TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationDetransformationPayload(Optional.of(target.getUUID()), kamikotizationData, false));
                    AbilityEffectData.removeFaceMaskTexture(target, kamikotizationData.kamikoData().faceMaskTexture());
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetSpectationInterruptedPayload(Optional.empty()));
            }, Button.DEFAULT_NARRATION) {
                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    if (active) {
                        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                            revokeButton.onPress();
                            return true;
                        }
                    }
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            };
            revokeButton.active = false;
        }
        return revokeButton;
    }

    public static void renderStealingProgressBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        int width = MineraculousKeyMappings.getTakeTicks();
        if (player != null && width > 0) {
            int x = (guiGraphics.guiWidth() - 18) / 2;
            int y = (guiGraphics.guiHeight() + 12) / 2;
            int max = MineraculousServerConfig.get().stealingDuration.get();
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + (width / max), y + 5, 0xFFFFFFF | -16777216);
        }
    }

    public static boolean checkRevokeButtonActive() {
        Player player = Minecraft.getInstance().player;
        boolean playerCanRevoke = player != null && player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).allowKamikotizationRevocation();
        Entity cameraEntity = MineraculousClientUtils.getCameraEntity();
        boolean entityCanHaveRevoked = cameraEntity != null && cameraEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || MineraculousClientUtils.isInKamikoView();
        return !kamikoGui.isMenuActive() && playerCanRevoke && entityCanHaveRevoked;
    }

    public static void renderRevokeButton(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Button revokeButton = getRevokeButton();
        if (checkRevokeButtonActive()) {
            if (MineraculousClientUtils.hasNoScreenOpen()) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 60);
                revokeButton.active = true;
                revokeButton.setMessage(REVOKE.copy().append(" ").append(Component.translatable(PRESS_KEY, MineraculousKeyMappings.REVOKE_KAMIKOTIZATION.getKey().getDisplayName())));
            } else if (Minecraft.getInstance().screen instanceof ChatScreen) {
                revokeButton.setPosition(revokeButton.getX(), Minecraft.getInstance().getWindow().getGuiScaledHeight() - 35);
                revokeButton.active = true;
                revokeButton.setMessage(REVOKE);
            } else {
                revokeButton.active = false;
            }
        } else {
            revokeButton.active = false;
        }

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
