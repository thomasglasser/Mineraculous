package dev.thomasglasser.mineraculous.api.client.gui.screens.look;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.components.tabs.ExtendedTabNavigationBar;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RegistryElementSelectionScreen;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataType;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

/**
 * Selects looks for a provided element and context set.
 * 
 * @param <T> The type of the element
 */
public class LookCustomizationScreen<T> extends Screen {
    public static final String SELECTED = "gui.mineraculous.look_customization.selected";
    public static final Component ENTER_NAME = Component.translatable("gui.mineraculous.look_customization.name").withStyle(ChatFormatting.GRAY);
    public static final Component APPLY = Component.translatable("gui.mineraculous.look_customization.apply");
    public static final Component UNDO = Component.translatable("gui.mineraculous.look_customization.undo");
    public static final Component RESET = Component.translatable("gui.mineraculous.look_customization.reset");

    private static final double MOUSE_SENSITIVITY_ROTATION_FACTOR = 1.5;
    private static final double ROTATION_SPEED = 1.3;
    private static final int MOUSE_SENSITIVITY_ZOOM_FACTOR = 5;
    private static final int MAX_ZOOM_LIMIT = 130;
    private static final int MIN_ZOOM_LIMIT = -40;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

    private final ImmutableSet<Holder<LookContext>> contextSet;
    private final LookMetadataType<Set<ResourceKey<T>>> metadataType;
    private final Holder<T> selected;
    private final Function<Player, LookData> lookDataGetter;
    private final BiConsumer<Player, LookData> lookDataSetter;
    private final BiConsumer<Player, LookData> onApply;

    private ImmutableMultimap<ResourceKey<LookContext>, ResourceLocation> looks;
    private Map<ResourceKey<LookContext>, ResourceLocation> selectedLooks;
    private PlayerPreview leftPreview;
    private PlayerPreview centerPreview;
    private PlayerPreview rightPreview;
    private ExtendedTabNavigationBar tabNavigationBar;
    private EditBox nameEdit;
    private Button previousButton;
    private Button nextButton;

    private boolean mouseAboveCenterPreview = false;
    private boolean mouseDragging = false;
    private double oldMouseX = 0;
    private double oldMouseY = 0;
    private double startHorizontalRotation = 0;
    private double startVerticalRotation = 0;
    private double selectedHorizontalRotation = 0;
    private double selectedVerticalRotation = 0;
    private double oldTickHorizontalRotation = 0;
    private double oldTickVerticalRotation = 0;
    private int zoom = 0;

    public LookCustomizationScreen(ImmutableSet<Holder<LookContext>> contextSet, LookMetadataType<Set<ResourceKey<T>>> metadataType, Holder<T> selected, Function<Player, LookData> lookDataGetter, BiConsumer<Player, LookData> lookDataSetter, BiConsumer<Player, LookData> onApply) {
        super(Component.empty());
        this.contextSet = contextSet;
        this.metadataType = metadataType;
        this.selected = selected;
        this.lookDataGetter = lookDataGetter;
        this.lookDataSetter = lookDataSetter;
        this.onApply = onApply;
    }

    public LookCustomizationScreen(ImmutableSet<Holder<LookContext>> contextSet, Supplier<LookMetadataType<Set<ResourceKey<T>>>> metadataType, Holder<T> selected, Function<Player, LookData> lookDataGetter, BiConsumer<Player, LookData> lookDataSetter, BiConsumer<Player, LookData> onApply) {
        this(contextSet, metadataType.get(), selected, lookDataGetter, lookDataSetter, onApply);
    }

    @Override
    protected void init() {
        super.init();

        this.nameEdit = new EditBox(font, 208, 20, ENTER_NAME);
        this.nameEdit.setHint(ENTER_NAME);

        refreshLooks();

        LinearLayout header = this.layout.addToHeader(LinearLayout.horizontal().spacing(8));
        header.addChild(Button.builder(Component.translatable(SELECTED, Component.translatable(MineraculousConstants.toLanguageKey(selected.getKey()))), button -> minecraft.setScreen(new RegistryElementSelectionScreen<>(this, selected.getKey().registryKey(), selected -> minecraft.setScreen(new LookCustomizationScreen<>(contextSet, metadataType, selected, lookDataGetter, lookDataSetter, onApply))))).build());
        header.addChild(this.nameEdit);
        ExtendedTabNavigationBar.Builder tabBuilder = ExtendedTabNavigationBar.builder(this.tabManager, this.width);
        for (Holder<LookContext> context : contextSet) {
            tabBuilder.addTabs(new LookTab(context));
        }
        this.tabNavigationBar = tabBuilder.build();
        this.addRenderableWidget(this.tabNavigationBar);
        previousButton = Button.builder(Component.literal("<"), button -> previousLook())
                .size(20, 20)
                .build();
        this.addRenderableWidget(previousButton);
        nextButton = Button.builder(Component.literal(">"), button -> nextLook())
                .size(20, 20)
                .build();
        this.addRenderableWidget(nextButton);
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(APPLY, button -> apply()).width(80).build());
        footer.addChild(Button.builder(UNDO, button -> undo()).width(80).build());
        footer.addChild(Button.builder(RESET, button -> reset()).width(80).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).width(80).build());
        this.layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setStartY(this.layout.getHeaderHeight());
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
            int i = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle tabArea = new ScreenRectangle(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
            this.tabManager.setTabArea(tabArea);
            this.previousButton.setPosition(this.width / 2 - 80, this.height / 2);
            this.nextButton.setPosition(this.width / 2 + 60, this.height / 2);
            this.layout.arrangeElements();
        }
    }

    @Override
    public void tick() {
        super.tick();
        oldTickHorizontalRotation = selectedHorizontalRotation;
        oldTickVerticalRotation = selectedVerticalRotation;
        if (!mouseDragging) {
            selectedHorizontalRotation -= ROTATION_SPEED;
            startHorizontalRotation = selectedHorizontalRotation;
            startVerticalRotation = selectedVerticalRotation;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int top = this.tabNavigationBar.getRectangle().bottom();
        int width = this.width / 3;
        int bottom = this.height - this.layout.getFooterHeight();

        float horizontalRotation = (float) Mth.lerp(partialTick, oldTickHorizontalRotation, selectedHorizontalRotation);
        float verticalRotation = (float) Mth.lerp(partialTick, oldTickVerticalRotation, selectedVerticalRotation);

        MineraculousClientUtils.renderEntityInInventory(guiGraphics, 0, top, width, bottom, 80, 0, 0, leftPreview);
        MineraculousClientUtils.renderEntityInInventory(guiGraphics, width, top, 2 * width, bottom, 80 + zoom, horizontalRotation, verticalRotation, centerPreview);
        MineraculousClientUtils.renderEntityInInventory(guiGraphics, 2 * width, top, 3 * width, bottom, 80, 0, 0, rightPreview);

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
        guiGraphics.flush();

        updateMouseAboveCenterPreview(mouseX, mouseY, width, top, 2 * width, bottom);
    }

    private void updateMouseAboveCenterPreview(int mouseX, int mouseY, int xStart, int yStart, int xEnd, int yEnd) {
        mouseAboveCenterPreview = mouseX < xEnd && mouseX > xStart && mouseY < yEnd && mouseY > yStart;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !isMouseOverWidget(mouseX, mouseY) && mouseAboveCenterPreview) {
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            mouseDragging = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (mouseDragging && button == 0) {
            selectedHorizontalRotation = oldMouseX - mouseX;
            selectedHorizontalRotation /= MOUSE_SENSITIVITY_ROTATION_FACTOR;
            selectedHorizontalRotation += startHorizontalRotation;

            selectedVerticalRotation = oldMouseY - mouseY;
            selectedVerticalRotation /= MOUSE_SENSITIVITY_ROTATION_FACTOR;
            selectedVerticalRotation += startVerticalRotation;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOverWidget(mouseX, mouseY) && mouseAboveCenterPreview) {
            zoom += (int) scrollY * MOUSE_SENSITIVITY_ZOOM_FACTOR;
            zoom = Math.max(MIN_ZOOM_LIMIT, zoom);
            zoom = Math.min(MAX_ZOOM_LIMIT, zoom);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.tabNavigationBar.keyPressed(keyCode)) {
            return true;
        } else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isMouseOverWidget(double mouseX, double mouseY) {
        for (var child : this.children()) {
            if (child instanceof AbstractWidget widget) {
                if (widget.isMouseOver(mouseX, mouseY) && widget.active && widget.visible) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void refreshLooks() {
        LocalPlayer player = (LocalPlayer) ClientUtils.getLocalPlayer();
        if (player == null)
            throw new IllegalStateException("Look Customization Screen has no player");
        LookData lookData = lookDataGetter.apply(player);
        lookData.name().ifPresent(nameEdit::setValue);
        this.selectedLooks = new Object2ObjectOpenHashMap<>(lookData.looks());

        ImmutableMultimap.Builder<ResourceKey<LookContext>, ResourceLocation> looks = new ImmutableMultimap.Builder<>();
        for (Holder<LookContext> context : contextSet) {
            Set<ResourceLocation> toCheck = new ObjectOpenHashSet<>(LookManager.getBuiltIn());
            for (String hash : LookManager.getEquippable()) {
                toCheck.add(ResourceLocation.withDefaultNamespace(hash));
            }
            for (ResourceLocation lookId : toCheck) {
                Look<?> look = LookManager.getLook(lookId);
                Set<ResourceKey<T>> valid = look.getMetadata(metadataType);
                if (valid != null && valid.contains(selected.getKey())) {
                    looks.put(context.getKey(), lookId);
                }
            }
        }
        this.looks = looks.build();

        if (selectedLooks.isEmpty()) {
            for (Holder<LookContext> context : contextSet) {
                ImmutableList<ResourceLocation> contextLooks = this.looks.get(context.getKey()).asList();
                if (!contextLooks.isEmpty())
                    selectedLooks.put(context.getKey(), contextLooks.getFirst());
            }
        }
    }

    protected void refreshPreviews() {
        LocalPlayer player = (LocalPlayer) ClientUtils.getLocalPlayer();
        if (player == null)
            throw new IllegalStateException("Look Customization Screen has no player");

        ClientLevel level = player.clientLevel;
        if (this.leftPreview != null) {
            leftPreview.discard();
            centerPreview.discard();
            rightPreview.discard();
        }
        this.leftPreview = new PlayerPreview(player);
        this.centerPreview = new PlayerPreview(player);
        this.rightPreview = new PlayerPreview(player);
        level.addEntity(leftPreview);
        level.addEntity(centerPreview);
        level.addEntity(rightPreview);

        LookTab currentTab = (LookTab) this.tabManager.getCurrentTab();
        if (currentTab != null) {
            Holder<LookContext> context = currentTab.getContext();
            context.value().preparePreview(leftPreview, selected);
            context.value().preparePreview(centerPreview, selected);
            context.value().preparePreview(rightPreview, selected);

            // Set looks on prepared previews
            ResourceKey<LookContext> contextKey = context.getKey();
            PlayerPreview[] previews = new PlayerPreview[] { leftPreview, centerPreview, rightPreview };
            ResourceLocation[] previewLooks = new ResourceLocation[] { getPreviousLook(contextKey), selectedLooks.get(contextKey), getNextLook(contextKey) };
            for (int i = 0; i < previews.length; i++) {
                PlayerPreview preview = previews[i];

                Map<ResourceKey<LookContext>, ResourceLocation> looks = new Object2ObjectOpenHashMap<>(selectedLooks);
                ResourceLocation lookId = previewLooks[i];
                if (lookId != null) {
                    looks.put(contextKey, lookId);
                }

                lookDataSetter.accept(preview, new LookData(Optional.empty(), ImmutableMap.copyOf(looks)));
            }
        }
    }

    protected void apply() {
        String name = nameEdit.getValue();
        onApply.accept(ClientUtils.getLocalPlayer(), new LookData(name.isBlank() ? Optional.empty() : Optional.of(name), ImmutableMap.copyOf(selectedLooks)));
        onClose();
    }

    protected void undo() {
        ResourceKey<LookContext> context = ((LookTab) tabManager.getCurrentTab()).getContext().getKey();
        selectedLooks.put(context, looks.get(context).asList().getFirst());
        refreshPreviews();
    }

    protected void reset() {
        refreshLooks();
        this.selectedLooks = new Object2ObjectOpenHashMap<>();
        refreshPreviews();
        zoom = 0;
        startHorizontalRotation = 0;
        startVerticalRotation = 0;
        selectedHorizontalRotation = 0;
        selectedVerticalRotation = 0;
        oldTickHorizontalRotation = 0;
        oldTickVerticalRotation = 0;
    }

    protected ResourceLocation getPreviousLook(ResourceKey<LookContext> context) {
        List<ResourceLocation> looks = this.looks.get(context).asList();
        int index = Math.max(0, looks.indexOf(selectedLooks.get(context)));
        if (index == 0) {
            return looks.getLast();
        }
        return looks.get(index - 1);
    }

    protected ResourceLocation getNextLook(ResourceKey<LookContext> context) {
        List<ResourceLocation> looks = this.looks.get(context).asList();
        int index = Math.max(0, looks.indexOf(selectedLooks.get(context)));
        if (index == looks.size() - 1) {
            return looks.getFirst();
        }
        return looks.get(index + 1);
    }

    protected void previousLook() {
        ResourceKey<LookContext> context = ((LookTab) tabManager.getCurrentTab()).getContext().getKey();
        ResourceLocation previous = getPreviousLook(context);
        selectedLooks.put(context, previous);
        refreshPreviews();
    }

    protected void nextLook() {
        ResourceKey<LookContext> context = ((LookTab) tabManager.getCurrentTab()).getContext().getKey();
        ResourceLocation next = getNextLook(context);
        selectedLooks.put(context, next);
        refreshPreviews();
    }

    protected void onTabChanged() {
        refreshPreviews();
    }

    @Override
    public void onClose() {
        super.onClose();
        leftPreview.discard();
        centerPreview.discard();
        rightPreview.discard();
    }

    protected class LookTab extends GridLayoutTab {
        private final Holder<LookContext> context;

        public LookTab(Holder<LookContext> context) {
            super(Component.translatable(MineraculousConstants.toLanguageKey(context.getKey())));
            this.context = context;
        }

        public Holder<LookContext> getContext() {
            return context;
        }

        @Override
        public void doLayout(ScreenRectangle rectangle) {
            super.doLayout(rectangle);
            onTabChanged();
        }
    }

    public static class PlayerPreview extends RemotePlayer {
        private final PlayerSkin skin;

        public PlayerPreview(LocalPlayer player) {
            super(player.clientLevel, new GameProfile(UUID.randomUUID(), ""));
            this.skin = player.getSkin();
        }

        @Override
        public PlayerSkin getSkin() {
            return skin;
        }

        @Override
        public boolean shouldRender(double x, double y, double z) {
            return false;
        }
    }
}
