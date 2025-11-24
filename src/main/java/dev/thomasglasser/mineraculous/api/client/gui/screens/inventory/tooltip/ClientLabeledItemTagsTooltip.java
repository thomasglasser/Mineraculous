package dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.tooltip;

import dev.thomasglasser.mineraculous.api.world.inventory.tooltip.LabeledItemTagsTooltip;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;

public class ClientLabeledItemTagsTooltip implements ClientTooltipComponent {
    private final Map<Component, TagKey<Item>> tagKeys;
    private Font font;

    public ClientLabeledItemTagsTooltip(Map<Component, TagKey<Item>> tagKeys) {
        this.tagKeys = tagKeys;
    }

    public ClientLabeledItemTagsTooltip(LabeledItemTagsTooltip tooltip) {
        this(tooltip.tagKeys());
    }

    @Override
    public int getHeight() {
        return font.lineHeight + 20;
    }

    @Override
    public int getWidth(Font font) {
        this.font = font;
        int width = -5;
        for (Component key : tagKeys.keySet()) {
            width += font.width(key) + 5;
        }
        return width;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        for (Component key : tagKeys.keySet()) {
            font.drawInBatch(key, x, y, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            x += font.width(key) + 5;
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        Player player = ClientUtils.getLocalPlayer();
        if (player != null) {
            Registry<Item> items = player.registryAccess().registryOrThrow(Registries.ITEM);
            for (Map.Entry<Component, TagKey<Item>> entry : tagKeys.entrySet()) {
                HolderSet.Named<Item> tag = items.getOrCreateTag(entry.getValue());
                guiGraphics.renderItem(tag.get(player.tickCount / 10 % tag.size()).value().getDefaultInstance(), x + (font.width(entry.getKey()) / 4), y + font.lineHeight);
                x += font.width(entry.getKey()) + 5;
            }
        }
    }
}
