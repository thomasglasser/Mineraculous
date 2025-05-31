package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundOpenPerformerKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundOpenVictimKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class KamikotizationSelectionScreen extends Screen {
    public static final ResourceLocation BACKGROUND = Mineraculous.modLoc("textures/gui/kamikotization_selection.png");
    public static final int BACKGROUND_WIDTH = 248;
    public static final int BACKGROUND_HEIGHT = 166;
    public static final Component TITLE = Component.translatable("gui.kamikotization.name");
    public static final Component TOOL = Component.translatable("gui.kamikotization.tool");
    public static final Component ACTIVE_ABILITY = Component.translatable("gui.kamikotization.active_ability");
    public static final Component PASSIVE_ABILITIES = Component.translatable("gui.kamikotization.passive_abilities");
    public static final String NO_KAMIKOTIZATIONS = "gui.kamikotization.no_kamikotizations";

    private final LinkedList<Holder<Kamikotization>> kamikotizations;
    private final Player target;
    private final Player targetPreview;
    private final KamikoData kamikoData;

    private int topLeftX;
    private int topLeftY;
    private Button selectOrDone;
    private EditBox name;
    private boolean showLeftArrow = false;
    private boolean showRightArrow = false;
    private boolean canScroll = false;
    private boolean scrolling = false;
    private int descStart = 0;

    @Nullable
    private Holder<Kamikotization> selectedKamikotization;

    public KamikotizationSelectionScreen(Player target, KamikoData kamikoData) {
        super(TITLE);
        this.kamikotizations = Kamikotization.getFor(target);
        this.target = target;
        this.targetPreview = new RemotePlayer((ClientLevel) target.level(), target.getGameProfile());
        this.kamikoData = kamikoData;
    }

    @Override
    protected void init() {
        clearWidgets();
        if (minecraft != null && minecraft.level != null) {
            if (selectedKamikotization == null && !kamikotizations.isEmpty())
                selectedKamikotization = kamikotizations.getFirst();
            if (selectedKamikotization == null) {
                onClose(true);
            }
            this.topLeftX = (this.width - BACKGROUND_WIDTH) / 2;
            this.topLeftY = (this.height - BACKGROUND_HEIGHT) / 2;
            this.name = new EditBox(this.font, topLeftX + 130, topLeftY + 7, 92, 17, Component.translatable(MineraculousClientUtils.NAME));
            this.name.setCanLoseFocus(true);
            this.name.setTextColor(-1);
            this.name.setTextColorUneditable(-1);
            this.name.setBordered(true);
            this.name.setMaxLength(50);
            this.name.setValue("");
            this.addWidget(this.name);
            this.name.setEditable(true);
            selectOrDone = Button.builder(Component.translatable(MineraculousClientUtils.CHOOSE), button -> onClose(false)).build();
            selectOrDone.setX(((this.width - BACKGROUND_WIDTH) / 2) + 50);
            selectOrDone.setY(((this.height - BACKGROUND_HEIGHT) / 2) + 175);
            addRenderableWidget(selectOrDone);
            onKamikotizationChanged();
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.name.getValue();
        this.init(minecraft, width, height);
        this.name.setValue(s);
    }

    public void renderBase(GuiGraphics guiGraphics) {
        guiGraphics.blit(BACKGROUND, topLeftX, topLeftY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBase(guiGraphics);
        this.renderArrows(guiGraphics, mouseX, mouseY);
        this.renderKamikotization(guiGraphics);
        this.renderScrollBar(guiGraphics);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderKamikotization(GuiGraphics guiGraphics) {
        if (targetPreview.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).isEmpty())
            targetPreview.setData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS, target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS));
        if (selectedKamikotization != null) {
            List<MutableComponent> components = new ArrayList<>();
            Either<ItemStack, Holder<Ability>> powerSource = selectedKamikotization.value().powerSource();
            if (powerSource.left().isPresent()) {
                components.add(TOOL.copy().withStyle(ChatFormatting.BOLD));
                components.add(powerSource.left().get().getHoverName().copy());
                components.add(Component.literal(""));
                targetPreview.setItemSlot(EquipmentSlot.MAINHAND, powerSource.left().get());
            } else if (powerSource.right().isPresent()) {
                components.add(ACTIVE_ABILITY.copy().withStyle(ChatFormatting.BOLD));
                components.add(Component.translatable(powerSource.right().get().getKey().location().toLanguageKey("ability")));
                components.add(Component.literal(""));
                targetPreview.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            if (!selectedKamikotization.value().passiveAbilities().isEmpty()) {
                components.add(PASSIVE_ABILITIES.copy().withStyle(ChatFormatting.UNDERLINE));
                components.addAll(selectedKamikotization.value().passiveAbilities().stream().map(holder -> Component.translatable(holder.getKey().location().toLanguageKey("ability"))).toList());
            }
            MineraculousClientUtils.renderEntityInInventorySpinning(guiGraphics, topLeftX + 15, topLeftY + 15, topLeftX + 113, topLeftY + 145, 60, (Minecraft.getInstance().player.tickCount % 360) * 2, targetPreview);
            guiGraphics.drawString(this.font, Component.literal("---------------"), topLeftX + 131, topLeftY + 22, Optional.ofNullable(ChatFormatting.WHITE.getColor()).orElseThrow(), false);
            for (int i = 0; i < components.size(); i++) {
                MutableComponent component = components.get(i);
                final int[] l = { i };
                List<FormattedCharSequence> lines = this.font.split(component, 90);
                lines.forEach(line -> {
                    if (l[0] < descStart + 7 && l[0] >= descStart)
                        guiGraphics.drawString(this.font, line, topLeftX + 131, topLeftY + 31 + ((l[0] - descStart) * 10), component.getStyle().getColor() != null ? component.getStyle().getColor().getValue() : Optional.ofNullable(ChatFormatting.WHITE.getColor()).orElseThrow(), false);
                    l[0]++;
                });
                canScroll = l[0] > 7;
            }
        }
    }

    protected void renderScrollBar(GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(this.canScroll ? CreativeModeInventoryScreen.SCROLLER_SPRITE : CreativeModeInventoryScreen.SCROLLER_DISABLED_SPRITE, topLeftX + 228, topLeftY + 7 + this.descStart, 12, 15);
    }

    protected void renderArrows(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int k = topLeftX + 10;
        int l = topLeftY + 135;
        if (showLeftArrow) guiGraphics.blit(BACKGROUND, k, l, !insideLeftArrow(mouseX, mouseY) ? 26 : 0, 166, 13, 20);
        k = topLeftX + 102;
        if (showRightArrow) guiGraphics.blit(BACKGROUND, k, l, 13 + (!insideRightArrow(mouseX, mouseY) ? 26 : 0), 166, 13, 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected boolean insideScrollbar(double mouseX, double mouseY) {
        int k = topLeftX + 227;
        int l = topLeftY + 47;
        int m = topLeftX + 240;
        int n = topLeftY + 157;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideLeftArrow(double mouseX, double mouseY) {
        int k = topLeftX + 10;
        int l = topLeftY + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideRightArrow(double mouseX, double mouseY) {
        int k = topLeftX + 102;
        int l = topLeftY + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && descStart >= 0 && descStart <= 95) {
            int j = ((this.height - BACKGROUND_HEIGHT) / 2) + 48;
            descStart = (int) mouseY - j;
            if (descStart < 0) descStart = 0;
            else if (descStart > 95) descStart = 95;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.insideScrollbar(mouseX, mouseY)) {
                this.scrolling = this.canScroll;
                return true;
            } else if (this.insideLeftArrow(mouseX, mouseY) && showLeftArrow) {
                return previousKamikotization();
            } else if (this.insideRightArrow(mouseX, mouseY) && showRightArrow) {
                return nextKamikotization();
            } else if (!this.name.isMouseOver(mouseX, mouseY)) {
                this.name.setFocused(false);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (canScroll && descStart >= 0 && descStart <= 95) {
            if (scrollY < 0) this.descStart++;
            else if (scrollY > 0) this.descStart--;
            if (descStart < 0) descStart = 0;
            else if (descStart > 95) descStart = 95;
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double delta) {
        return mouseScrolled(0, 0, 0, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            selectOrDone.onPress();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT)
            return previousKamikotization();
        else if (keyCode == GLFW.GLFW_KEY_RIGHT)
            return nextKamikotization();
        else if (keyCode == GLFW.GLFW_KEY_UP)
            return mouseScrolled(10);
        else if (keyCode == GLFW.GLFW_KEY_DOWN)
            return mouseScrolled(-10);

        return this.name.keyPressed(keyCode, scanCode, modifiers) || this.name.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void refreshArrows() {
        showLeftArrow = selectedKamikotization != null && selectedKamikotization != kamikotizations.getFirst();
        showRightArrow = selectedKamikotization != null && selectedKamikotization != kamikotizations.getLast();
    }

    protected boolean nextKamikotization() {
        if (selectedKamikotization != null) {
            int index = kamikotizations.indexOf(selectedKamikotization);
            if (index < kamikotizations.size() - 1) {
                selectedKamikotization = kamikotizations.get(index + 1);
                onKamikotizationChanged();
                return true;
            }
        }
        return false;
    }

    protected boolean previousKamikotization() {
        if (selectedKamikotization != null) {
            int index = kamikotizations.indexOf(selectedKamikotization);
            if (index > 0) {
                selectedKamikotization = kamikotizations.get(index - 1);
                onKamikotizationChanged();
                return true;
            }
        }
        return false;
    }

    protected void onKamikotizationChanged() {
        if (selectedKamikotization != null) {
            targetPreview.setItemSlot(EquipmentSlot.HEAD, Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.HEAD.get(), selectedKamikotization.getKey()));
            targetPreview.setItemSlot(EquipmentSlot.CHEST, Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.CHEST.get(), selectedKamikotization.getKey()));
            targetPreview.setItemSlot(EquipmentSlot.LEGS, Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.LEGS.get(), selectedKamikotization.getKey()));
            targetPreview.setItemSlot(EquipmentSlot.FEET, Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.FEET.get(), selectedKamikotization.getKey()));
            name.setValue(selectedKamikotization.value().defaultName());
        }
        refreshArrows();
    }

    public void onClose(boolean cancel) {
        super.onClose();
        LocalPlayer player = this.minecraft.player;
        if (selectedKamikotization == null) {
            player.displayClientMessage(Component.translatable(NO_KAMIKOTIZATIONS, target.getDisplayName()), true);
            cancel = true;
        }
        if (cancel) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(player.getUUID(), target.blockPosition().above()));
            AbilityEffectData.checkRemoveFaceMaskTexture(player, kamikoData.faceMaskTexture());
        } else {
            minecraft.setScreen(new ExternalCuriosInventoryScreen(target, false, new ExternalInventoryScreen.ItemPickupHandler() {
                @Override
                public boolean canPickUp(Slot slot, Player target, AbstractContainerMenu menu) {
                    ItemStack stack = slot.getItem();
                    return selectedKamikotization.value().itemPredicate().test(stack) && !stack.has(MineraculousDataComponents.KAMIKOTIZATION);
                }

                @Override
                public void pickUp(Slot slot, Player target, AbstractContainerMenu menu) {
                    Either<Integer, CuriosData> slotInfo;
                    if (slot instanceof CurioSlot curiosSlot)
                        slotInfo = Either.right(new CuriosData(curiosSlot.getSlotIndex(), curiosSlot.getIdentifier()));
                    else
                        slotInfo = Either.left(slot.getSlotIndex());
                    KamikotizationData kamikotizationData = new KamikotizationData(selectedKamikotization.getKey(), slot.getItem().getCount(), slotInfo, kamikoData, false, Optional.of(Either.left(0)), name.getValue());
                    if (target == minecraft.player) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(kamikotizationData, true, false, false, minecraft.player.position().add(0, 1, 0)));
                        TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(target.getUUID(), target.getUUID(), kamikotizationData.kamikotization()));
                        AbilityEffectData.checkRemoveFaceMaskTexture(target, kamikoData.faceMaskTexture());
                    } else {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundOpenVictimKamikotizationChatScreenPayload(target.getUUID(), kamikotizationData));
                        MiraculousesData playerMiraculousSet = minecraft.player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                        TommyLibServices.NETWORK.sendToServer(new ServerboundOpenPerformerKamikotizationChatScreenPayload(playerMiraculousSet.get(playerMiraculousSet.getTransformed().getFirst()).name(), name.getValue(), kamikoData.faceMaskTexture(), target.getUUID()));
                    }
                }
            }, exit -> {
                if (exit) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(player.getUUID(), target.blockPosition().above()));
                    AbilityEffectData.checkRemoveFaceMaskTexture(target, kamikoData.faceMaskTexture());
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID(), false));
            }));
        }
    }

    @Override
    public void onClose() {
        onClose(true);
    }
}
