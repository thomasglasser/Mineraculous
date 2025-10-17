package dev.thomasglasser.mineraculous.api.world.inventory.tooltip;

import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiFoods;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

public record LabeledItemTagsTooltip(SortedMap<Component, TagKey<Item>> tagKeys) implements TooltipComponent {
    public LabeledItemTagsTooltip(KwamiFoods kwamiFoods) {
        this(Util.make(new Object2ReferenceLinkedOpenHashMap<>(), map -> {
            map.put(KwamiFoods.FOODS, kwamiFoods.foods());
            map.put(KwamiFoods.TREATS, kwamiFoods.treats());
        }));
    }
}
