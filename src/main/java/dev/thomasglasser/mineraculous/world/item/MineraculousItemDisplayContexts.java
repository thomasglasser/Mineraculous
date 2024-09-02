package dev.thomasglasser.mineraculous.world.item;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousItemDisplayContexts {
    public static final EnumProxy<ItemDisplayContext> CURIOS_RING = new EnumProxy<>(ItemDisplayContext.class,
            9,
            "mineraculous:curios_ring",
            null);
}
