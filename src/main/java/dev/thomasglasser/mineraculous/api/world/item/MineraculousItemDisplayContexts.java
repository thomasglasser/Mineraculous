package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousItemDisplayContexts {
    public static final EnumProxy<ItemDisplayContext> CURIOS_OTHER = new EnumProxy<>(ItemDisplayContext.class,
            9,
            "mineraculous:curios_other",
            null);
    public static final EnumProxy<ItemDisplayContext> CURIOS_RING = new EnumProxy<>(ItemDisplayContext.class,
            10,
            "mineraculous:curios_ring",
            null);
    public static final EnumProxy<ItemDisplayContext> CURIOS_BROOCH = new EnumProxy<>(ItemDisplayContext.class,
            11,
            "mineraculous:curios_brooch",
            null);
    public static final EnumProxy<ItemDisplayContext> CURIOS_EARRINGS = new EnumProxy<>(ItemDisplayContext.class,
            12,
            "mineraculous:curios_earrings",
            null);
    public static final EnumProxy<ItemDisplayContext> CURIOS_BELT = new EnumProxy<>(ItemDisplayContext.class,
            13,
            "mineraculous:curios_belt",
            null);
}
