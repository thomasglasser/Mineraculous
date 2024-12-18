package dev.thomasglasser.mineraculous.client.gui.screens;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundOpenPerformerKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundOpenVictimKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetToggleTagPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class KamikotizationSelectionScreen extends Screen {
    public static final String TITLE = "gui.kamikotization.name";
    public static final String NO_KAMIKOTIZATIONS = "gui.kamikotization.no_kamikotizations";
    public static final String CHOOSE = "gui.choose";
    public static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    public static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private static final ResourceLocation BACKGROUND = Mineraculous.modLoc("textures/gui/kamikotization_selection.png");
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private final List<Holder<Kamikotization>> kamikotizations;

    private final Player target;
    private final Player targetPreview;
    private final KamikoData kamikoData;

    private boolean showLeftArrow = false;
    private boolean showRightArrow = false;
    private boolean canScroll = false;
    private boolean scrolling = false;
    private int descStart = 0;
    private int rotation = 0;
    private Button selectOrDone;
    private EditBox name;

    @Nullable
    private Holder<Kamikotization> selectedKamikotization;

    public KamikotizationSelectionScreen(Component pTitle, Player target, KamikoData kamikoData) {
        super(pTitle);
        this.kamikotizations = Kamikotization.getFor(target).stream().toList();
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
            int i = (this.width - BACKGROUND_WIDTH) / 2;
            int j = (this.height - BACKGROUND_HEIGHT) / 2;
            this.name = new EditBox(this.font, i + 130, j + 7, 92, 17, Component.translatable("container.repair"));
            this.name.setCanLoseFocus(true);
            this.name.setTextColor(-1);
            this.name.setTextColorUneditable(-1);
            this.name.setBordered(true);
            this.name.setMaxLength(50);
            this.name.setValue("");
            this.addWidget(this.name);
            this.name.setEditable(true);
            selectOrDone = Button.builder(Component.translatable(CHOOSE), button -> {
                onClose(false);
            }).build();
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
        int i = (this.width - BACKGROUND_WIDTH) / 2;
        int j = (this.height - BACKGROUND_HEIGHT) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
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
        if (selectedKamikotization != null) {
            List<MutableComponent> abilities = selectedKamikotization.value().abilities().stream().map(holder -> Component.translatable(holder.getKey().location().toLanguageKey("ability"))).toList();

            int x = (this.width - BACKGROUND_WIDTH) / 2;
            int y = (this.height - BACKGROUND_HEIGHT) / 2;
            renderEntityInInventorySpinning(guiGraphics, x + 15, y + 15, x + 113, y + 145, 60, rotation, targetPreview);
            rotation++;
            guiGraphics.drawString(this.font, Component.literal("---------------"), x + 131, y + 22, Optional.ofNullable(ChatFormatting.WHITE.getColor()).orElseThrow(), false);
            for (int i = 0; i < abilities.size(); i++) {
                MutableComponent ability = abilities.get(i);
                final int[] l = { i };
                List<FormattedCharSequence> nameLines = this.font.split(ability, 90);
                nameLines.forEach(formattedCharSequence -> {
                    if (l[0] < descStart + 7 && l[0] >= descStart)
                        guiGraphics.drawString(this.font, formattedCharSequence, x + 131, y + 31 + ((l[0] - descStart) * 10), ability.getStyle().getColor() != null ? ability.getStyle().getColor().getValue() : Optional.ofNullable(ChatFormatting.WHITE.getColor()).orElseThrow(), false);
                    l[0]++;
                });
                canScroll = l[0] > 7;
            }
        }
    }

    public static void renderEntityInInventorySpinning(
            GuiGraphics guiGraphics,
            int xStart,
            int yStart,
            int xEnd,
            int yEnd,
            int scale,
            float rotation,
            LivingEntity entity) {
        float x = (float) (xStart + xEnd) / 2.0F;
        float y = (float) (yStart + yEnd) / 2.0F;
        guiGraphics.enableScissor(xStart, yStart, xEnd, yEnd);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(90 * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf1);
        float f4 = entity.yBodyRot;
        float f5 = entity.getYRot();
        float f6 = entity.getXRot();
        float f7 = entity.yHeadRotO;
        float f8 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + rotation * 2;
        entity.setYRot(180.0F + rotation * 2);
        entity.setXRot(-90 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float f9 = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * f9, 0.0F);
        float f10 = (float) scale / f9;
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, f10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        guiGraphics.disableScissor();
    }

    protected void renderScrollBar(GuiGraphics guiGraphics) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 228;
        int l = j + 7;
        ResourceLocation resourceLocation = this.canScroll ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(resourceLocation, k, l + this.descStart, 12, 15);
    }

    protected void renderArrows(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 10;
        int l = j + 135;
        if (showLeftArrow) guiGraphics.blit(BACKGROUND, k, l, !insideLeftArrow(mouseX, mouseY) ? 26 : 0, 166, 13, 20);
        k = i + 102;
        if (showRightArrow) guiGraphics.blit(BACKGROUND, k, l, 13 + (!insideRightArrow(mouseX, mouseY) ? 26 : 0), 166, 13, 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected boolean insideScrollbar(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 227;
        int l = j + 47;
        int m = i + 240;
        int n = j + 157;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideLeftArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 10;
        int l = j + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideRightArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 102;
        int l = j + 135;
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
        showLeftArrow = kamikotizations.indexOf(selectedKamikotization) > 0;
        showRightArrow = kamikotizations.indexOf(selectedKamikotization) < kamikotizations.size() - 1;
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
        rotation = 0;
    }

    public void onClose(boolean cancel) {
        super.onClose();
        if (selectedKamikotization == null) {
            ClientUtils.getMainClientPlayer().displayClientMessage(Component.translatable(NO_KAMIKOTIZATIONS, target.getDisplayName()), true);
            cancel = true;
        }
        if (cancel) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(ClientUtils.getMainClientPlayer().getUUID(), target.blockPosition().above()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
        } else {
            ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target, false, (slot, target, menu) -> {
                Either<Integer, CuriosData> slotInfo;
                if (slot instanceof CurioSlot curiosSlot)
                    slotInfo = Either.right(new CuriosData(curiosSlot.getSlotIndex(), curiosSlot.getIdentifier()));
                else
                    slotInfo = Either.left(target.getInventory().findSlotMatchingItem(slot.getItem()));
                KamikotizationData kamikotizationData = new KamikotizationData(Optional.ofNullable(selectedKamikotization.getKey()), slot.getItem(), slotInfo, kamikoData, name.getValue());
                if (target == ClientUtils.getMainClientPlayer()) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(kamikotizationData, true));
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
                } else {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundOpenVictimKamikotizationChatScreenPayload(target.getUUID(), kamikotizationData));
                    MiraculousDataSet playerMiraculousSet = ClientUtils.getMainClientPlayer().getData(MineraculousAttachmentTypes.MIRACULOUS);
                    TommyLibServices.NETWORK.sendToServer(new ServerboundOpenPerformerKamikotizationChatScreenPayload(name.getValue(), playerMiraculousSet.get(playerMiraculousSet.getTransformed().getFirst()).name(), target.getUUID()));
                }
            }, exit -> {
                if (exit) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(ClientUtils.getMainClientPlayer().getUUID(), target.blockPosition().above()));
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
                }
            }));
        }
    }

    @Override
    public void onClose() {
        onClose(true);
    }
}
