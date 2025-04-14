package dev.thomasglasser.mineraculous.client.gui.screens;

import com.google.common.collect.HashBasedTable;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.core.MineraculousCoreEvents;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundSyncCustomizationPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.lwjgl.glfw.GLFW;

public class LookCustomizationScreen extends Screen {
    public static final String TITLE = "gui.look.name";

    private static final ResourceLocation BACKGROUND = Mineraculous.modLoc("textures/gui/look_customization.png");
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private final ResourceKey<Miraculous> miraculous;
    private final Map<String, FlattenedSuitLookData> flattenedSuitLooks;
    private final Map<String, FlattenedMiraculousLookData> flattenedMiraculousLooks;
    private final List<Map.Entry<String, SuitLookData>> suitLooks;
    private final List<Map.Entry<String, MiraculousLookData>> miraculousLooks;
    private final Player previewSuit;
    private final Player previewMiraculousPlayer;
    private final ItemStack previewMiraculousStack;

    private boolean showLeftSuitArrow = false;
    private boolean showRightSuitArrow = false;
    private boolean showLeftMiraculousArrow = false;
    private boolean showRightMiraculousArrow = false;
    private Button done;
    private EditBox name;

    private Map.Entry<String, SuitLookData> selectedSuit;
    private Map.Entry<String, MiraculousLookData> selectedMiraculous;

    public LookCustomizationScreen(ResourceKey<Miraculous> miraculous, Map<String, FlattenedSuitLookData> serverSuits, Map<String, FlattenedMiraculousLookData> serverMiraculous) {
        super(Component.translatable(TITLE, Component.translatable(Miraculous.toLanguageKey(miraculous))));
        this.miraculous = miraculous;
        this.flattenedSuitLooks = getFlattenedSuitLooks(serverSuits);
        this.flattenedMiraculousLooks = getFlattenedMiraculousLooks(serverMiraculous);
        this.suitLooks = getSuitLooks(flattenedSuitLooks);
        this.miraculousLooks = getMiraculousLooks(flattenedMiraculousLooks);
        this.previewSuit = new RemotePlayer(Minecraft.getInstance().level, Minecraft.getInstance().getGameProfile());
        this.previewSuit.setItemSlot(EquipmentSlot.HEAD, Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), miraculous));
        this.previewSuit.setItemSlot(EquipmentSlot.CHEST, Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.CHEST.get(), miraculous));
        this.previewSuit.setItemSlot(EquipmentSlot.LEGS, Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.LEGS.get(), miraculous));
        this.previewSuit.setItemSlot(EquipmentSlot.FEET, Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.FEET.get(), miraculous));
        CuriosUtils.setStackInFirstValidSlot(this.previewSuit, Miraculous.createMiraculousStack(miraculous));
        HashBasedTable<ResourceKey<Miraculous>, String, SuitLookData> previewSuitLooks = HashBasedTable.create();
        for (Map.Entry<String, SuitLookData> entry : suitLooks) {
            previewSuitLooks.put(miraculous, entry.getKey(), entry.getValue());
        }
        this.previewSuit.setData(MineraculousAttachmentTypes.MIRACULOUS_SUIT_LOOKS, previewSuitLooks);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        this.previewMiraculousPlayer = new RemotePlayer(Minecraft.getInstance().level, profile) {
            @Override
            public boolean shouldRender(double x, double y, double z) {
                return false;
            }
        };
        HashBasedTable<ResourceKey<Miraculous>, String, MiraculousLookData> previewMiraculousLooks = HashBasedTable.create();
        for (Map.Entry<String, MiraculousLookData> entry : miraculousLooks) {
            previewMiraculousLooks.put(miraculous, entry.getKey(), entry.getValue());
        }
        this.previewMiraculousPlayer.setData(MineraculousAttachmentTypes.MIRACULOUS_MIRACULOUS_LOOKS, previewMiraculousLooks);
        Minecraft.getInstance().level.addEntity(previewMiraculousPlayer);
        this.previewMiraculousStack = Miraculous.createMiraculousStack(miraculous);
        this.previewMiraculousStack.remove(MineraculousDataComponents.POWERED);
        this.previewMiraculousStack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
    }

    protected Map<String, FlattenedSuitLookData> getFlattenedSuitLooks(Map<String, FlattenedSuitLookData> serverSuits) {
        Map<String, FlattenedSuitLookData> suitLooks = new HashMap<>(serverSuits);
        if (MineraculousServerConfig.get().enableCustomization.get()) {
            for (Map.Entry<String, FlattenedSuitLookData> entry : MineraculousCoreEvents.fetchSuitLooks(Minecraft.getInstance().gameDirectory.toPath(), Minecraft.getInstance().level.holderOrThrow(miraculous)).entrySet()) {
                suitLooks.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return suitLooks;
    }

    protected List<Map.Entry<String, SuitLookData>> getSuitLooks(Map<String, FlattenedSuitLookData> flattenedSuitLooks) {
        Map<String, SuitLookData> suitLooks = new HashMap<>();
        suitLooks.put("", new SuitLookData(Optional.empty(), ResourceLocation.withDefaultNamespace(""), Optional.empty(), List.of(), List.of(), Optional.empty()));
        for (Map.Entry<String, FlattenedSuitLookData> entry : flattenedSuitLooks.entrySet()) {
            SuitLookData data = entry.getValue() == null ? null : entry.getValue().unpack(miraculous, Minecraft.getInstance().player);
            if (data != null)
                suitLooks.put(entry.getKey(), data);
        }
        return suitLooks.entrySet().stream().toList();
    }

    protected Map<String, FlattenedMiraculousLookData> getFlattenedMiraculousLooks(Map<String, FlattenedMiraculousLookData> serverMiraculous) {
        Map<String, FlattenedMiraculousLookData> miraculousLooks = new HashMap<>(serverMiraculous);
        if (MineraculousServerConfig.get().enableCustomization.get()) {
            for (Map.Entry<String, FlattenedMiraculousLookData> entry : MineraculousCoreEvents.fetchMiraculousLooks(Minecraft.getInstance().gameDirectory.toPath(), miraculous).entrySet()) {
                miraculousLooks.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return miraculousLooks;
    }

    protected List<Map.Entry<String, MiraculousLookData>> getMiraculousLooks(Map<String, FlattenedMiraculousLookData> flattenedMiraculousLooks) {
        Map<String, MiraculousLookData> miraculousLooks = new HashMap<>();
        miraculousLooks.put("", new MiraculousLookData(Optional.empty(), ResourceLocation.withDefaultNamespace(""), Optional.empty(), Optional.empty()));
        for (Map.Entry<String, FlattenedMiraculousLookData> entry : flattenedMiraculousLooks.entrySet()) {
            MiraculousLookData data = entry.getValue() == null ? null : entry.getValue().unpack(miraculous, Minecraft.getInstance().player);
            if (data != null)
                miraculousLooks.put(entry.getKey(), data);
        }
        return miraculousLooks.entrySet().stream().toList();
    }

    @Override
    protected void init() {
        clearWidgets();
        if (minecraft != null && minecraft.level != null) {
            if (selectedSuit == null) {
                Map.Entry<String, SuitLookData> currentSuit = suitLooks.stream().filter(entry -> entry.getKey().equals(minecraft.player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).suitLook())).findFirst().orElse(null);
                if (currentSuit != null)
                    selectedSuit = currentSuit;
                else
                    selectedSuit = suitLooks.getFirst();
            }
            if (selectedMiraculous == null) {
                Map.Entry<String, MiraculousLookData> currentMiraculous = miraculousLooks.stream().filter(entry -> entry.getKey().equals(minecraft.player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).miraculousLook())).findFirst().orElse(null);
                if (currentMiraculous != null)
                    selectedMiraculous = currentMiraculous;
                else
                    selectedMiraculous = miraculousLooks.getFirst();
            }
            int i = (this.width - BACKGROUND_WIDTH) / 2;
            int j = (this.height - BACKGROUND_HEIGHT) / 2;
            this.name = new EditBox(this.font, i + 130, j + 7, 109, 17, Component.translatable(MineraculousClientUtils.NAME));
            this.name.setCanLoseFocus(true);
            this.name.setTextColor(Minecraft.getInstance().level.holderOrThrow(miraculous).value().color().getValue());
            this.name.setTextColorUneditable(-1);
            this.name.setBordered(true);
            this.name.setMaxLength(50);
            this.name.setValue(Minecraft.getInstance().player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous).name());
            this.addWidget(this.name);
            this.name.setEditable(true);
            done = Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build();
            done.setX(((this.width - BACKGROUND_WIDTH) / 2) + 50);
            done.setY(((this.height - BACKGROUND_HEIGHT) / 2) + 175);
            addRenderableWidget(done);
            onSuitChanged();
            onMiraculousChanged();
        }
    }

    protected void onLookChanged() {
        refreshArrows();
    }

    protected void refreshArrows() {
        showLeftSuitArrow = selectedSuit != suitLooks.getFirst();
        showRightSuitArrow = selectedSuit != suitLooks.getLast();
        showLeftMiraculousArrow = selectedMiraculous != miraculousLooks.getFirst();
        showRightMiraculousArrow = selectedMiraculous != miraculousLooks.getLast();
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
        this.renderSuit(guiGraphics);
        this.renderMiraculous(guiGraphics);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderSuit(GuiGraphics guiGraphics) {
        int x = (this.width - BACKGROUND_WIDTH) / 2;
        int y = (this.height - BACKGROUND_HEIGHT) / 2;
        MineraculousClientUtils.renderEntityInInventorySpinning(guiGraphics, x + 15, y + 15, x + 113, y + 145, 60, (Minecraft.getInstance().player.tickCount % 360) * 2, previewSuit);
    }

    public void renderMiraculous(GuiGraphics guiGraphics) {
        int x = (this.width - BACKGROUND_WIDTH) / 2;
        int y = (this.height - BACKGROUND_HEIGHT) / 2;
        BakedModel bakedmodel = this.minecraft.getItemRenderer().getModel(previewMiraculousStack, Minecraft.getInstance().level, null, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + 185, y + 90, 0.0F);
        guiGraphics.pose().scale(5, 5, 5);
        guiGraphics.pose().scale(16, -16, 16);
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        guiGraphics.pose().mulPose(Axis.YN.rotationDegrees((Minecraft.getInstance().player.tickCount % 360) * 4));
        this.minecraft.getItemRenderer().render(previewMiraculousStack, ItemDisplayContext.GUI, false, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        guiGraphics.flush();
        if (flag) {
            Lighting.setupFor3DItems();
        }
        guiGraphics.pose().popPose();
    }

    protected void renderArrows(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 10;
        int l = j + 135;
        if (showLeftSuitArrow) guiGraphics.blit(BACKGROUND, k, l, !insideLeftSuitArrow(mouseX, mouseY) ? 26 : 0, 166, 13, 20);
        k = i + 102;
        if (showRightSuitArrow) guiGraphics.blit(BACKGROUND, k, l, 13 + (!insideRightSuitArrow(mouseX, mouseY) ? 26 : 0), 166, 13, 20);
        k = i + 130;
        l = j + 135;
        if (showLeftMiraculousArrow) guiGraphics.blit(BACKGROUND, k, l, !insideLeftMiraculousArrow(mouseX, mouseY) ? 26 : 0, 166, 13, 20);
        k = i + 224;
        if (showRightMiraculousArrow) guiGraphics.blit(BACKGROUND, k, l, 13 + (!insideRightMiraculousArrow(mouseX, mouseY) ? 26 : 0), 166, 13, 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected boolean insideLeftSuitArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 10;
        int l = j + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideRightSuitArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 102;
        int l = j + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideLeftMiraculousArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 130;
        int l = j + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    protected boolean insideRightMiraculousArrow(double mouseX, double mouseY) {
        int i = ((this.width - BACKGROUND_WIDTH) / 2);
        int j = ((this.height - BACKGROUND_HEIGHT) / 2);
        int k = i + 224;
        int l = j + 135;
        int m = k + 18;
        int n = l + 20;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.insideLeftSuitArrow(mouseX, mouseY) && showLeftSuitArrow) {
                return previousSuit();
            } else if (this.insideRightSuitArrow(mouseX, mouseY) && showRightSuitArrow) {
                return nextSuit();
            } else if (this.insideLeftMiraculousArrow(mouseX, mouseY) && showLeftMiraculousArrow) {
                return previousMiraculous();
            } else if (this.insideRightMiraculousArrow(mouseX, mouseY) && showRightMiraculousArrow) {
                return nextMiraculous();
            } else if (!this.name.isMouseOver(mouseX, mouseY)) {
                this.name.setFocused(false);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            done.onPress();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose(true);
            return true;
        }

        return this.name.keyPressed(keyCode, scanCode, modifiers) || this.name.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected boolean nextSuit() {
        if (selectedSuit != null) {
            int index = suitLooks.indexOf(selectedSuit);
            if (index < suitLooks.size() - 1) {
                selectedSuit = suitLooks.get(index + 1);
                onSuitChanged();
                return true;
            }
        }
        return false;
    }

    protected boolean previousSuit() {
        if (selectedSuit != null) {
            int index = suitLooks.indexOf(selectedSuit);
            if (index > 0) {
                selectedSuit = suitLooks.get(index - 1);
                onSuitChanged();
                return true;
            }
        }
        return false;
    }

    protected boolean nextMiraculous() {
        if (selectedMiraculous != null) {
            int index = miraculousLooks.indexOf(selectedMiraculous);
            if (index < miraculousLooks.size() - 1) {
                selectedMiraculous = miraculousLooks.get(index + 1);
                onMiraculousChanged();
                return true;
            }
        }
        return false;
    }

    protected boolean previousMiraculous() {
        if (selectedMiraculous != null) {
            int index = miraculousLooks.indexOf(selectedMiraculous);
            if (index > 0) {
                selectedMiraculous = miraculousLooks.get(index - 1);
                onMiraculousChanged();
                return true;
            }
        }
        return false;
    }

    protected void onSuitChanged() {
        onLookChanged();
        if (selectedSuit != null) {
            MiraculousDataSet miraculousDataSet = previewSuit.getData(MineraculousAttachmentTypes.MIRACULOUS);
            miraculousDataSet.put(previewSuit, miraculous, miraculousDataSet.get(miraculous).withSuitLook(selectedSuit.getKey()), false);
        }
    }

    protected void onMiraculousChanged() {
        onLookChanged();
        if (selectedMiraculous != null) {
            MiraculousDataSet miraculousDataSet = previewMiraculousPlayer.getData(MineraculousAttachmentTypes.MIRACULOUS);
            miraculousDataSet.put(previewMiraculousPlayer, miraculous, miraculousDataSet.get(miraculous).withMiraculousLook(selectedMiraculous.getKey()), false);
        }
    }

    public void onClose(boolean cancel) {
        super.onClose();
        Minecraft.getInstance().level.removeEntity(previewMiraculousPlayer.getId(), Entity.RemovalReason.DISCARDED);
        if (!cancel) {
            FlattenedSuitLookData flattenedSuitLookData = null;
            if (selectedSuit != null && !selectedSuit.getKey().isEmpty())
                flattenedSuitLookData = flattenedSuitLooks.get(selectedSuit.getKey());
            FlattenedMiraculousLookData flattenedMiraculousLookData = null;
            if (selectedMiraculous != null && !selectedMiraculous.getKey().isEmpty())
                flattenedMiraculousLookData = flattenedMiraculousLooks.get(selectedMiraculous.getKey());
            TommyLibServices.NETWORK.sendToServer(new ServerboundSyncCustomizationPayload(miraculous, name.getValue(), Optional.ofNullable(flattenedSuitLookData), Optional.ofNullable(flattenedMiraculousLookData)));
        }
    }

    @Override
    public void onClose() {
        onClose(false);
    }
}
