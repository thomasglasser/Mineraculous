package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.ibm.icu.text.Collator;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Selects an element of a registry from a list of all registry entries.
 * 
 * @param <T> The type of the element
 */
public class RegistryElementSelectionScreen<T> extends Screen {
    public static final String TITLE = "gui.mineraculous.registry_element_selection.title";

    private static final int SPACING = 8;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final Consumer<Holder<T>> applySettings;
    final Registry<T> registry;
    private ElementList list;
    Holder<T> selected;
    private Button doneButton;

    public RegistryElementSelectionScreen(Screen parent, ResourceKey<Registry<T>> registryKey, Consumer<Holder<T>> applySettings) {
        super(Component.translatable(TITLE, Component.translatable(MineraculousConstants.toLanguageKey(registryKey))));
        this.parent = parent;
        this.applySettings = applySettings;
        Level level = ClientUtils.getLevel();
        if (level == null)
            throw new IllegalStateException("Cannot create RegistryElementSelectionScreen without a level");
        this.registry = level.registryAccess().registryOrThrow(registryKey);
        this.selected = this.registry.holders().findFirst().orElseThrow();
    }

    @Override
    public final void onClose() {
        onClose(true);
    }

    public void onClose(boolean cancel) {
        if (cancel) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    protected void init() {
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(SPACING));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.getTitle(), this.font));
        this.list = this.layout.addToContents(new ElementList());
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(SPACING));
        this.doneButton = footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.selected);
            this.onClose(false);
        }).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.list.setSelected(this.list.children().stream().filter(entry -> Objects.equals(entry.element, this.selected)).findFirst().orElse(null));
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.list.updateSize(this.width, this.layout);
    }

    void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    private class ElementList extends ObjectSelectionList<ElementList.Entry> {
        private ElementList() {
            super(RegistryElementSelectionScreen.this.minecraft, RegistryElementSelectionScreen.this.width, RegistryElementSelectionScreen.this.height - 77, 40, 16);
            Collator collator = Collator.getInstance(Locale.getDefault());
            RegistryElementSelectionScreen.this.registry
                    .holders()
                    .map(Entry::new)
                    .sorted(Comparator.comparing(entry -> entry.name.getString(), collator))
                    .forEach(this::addEntry);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                RegistryElementSelectionScreen.this.selected = entry.element;
            }

            RegistryElementSelectionScreen.this.updateButtonValidity();
        }

        private class Entry extends ObjectSelectionList.Entry<ElementList.Entry> {
            final Holder.Reference<T> element;
            final Component name;

            public Entry(Holder.Reference<T> element) {
                this.element = element;
                String langKey = MineraculousConstants.toLanguageKey(element.key());
                if (Language.getInstance().has(langKey)) {
                    this.name = Component.translatable(langKey);
                } else {
                    this.name = Component.literal(element.key().location().toString());
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
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
                guiGraphics.drawString(RegistryElementSelectionScreen.this.font, this.name, left + 5, top + 2, 16777215);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ElementList.this.setSelected(this);
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }
    }
}
