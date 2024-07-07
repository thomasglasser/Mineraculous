package dev.thomasglasser.mineraculous.client.renderer.item;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.client.renderer.item.ItemProperties;

public class MineraculousItemProperties {
    public static void init() {
        ItemProperties.register(MineraculousItems.CAT_STAFF.get(), CatStaffItem.EXTENDED_PROPERTY_ID, (stack, level, entity, seed) -> stack.has(MineraculousDataComponents.POWERED) ? 1 : 0);
    }
}
