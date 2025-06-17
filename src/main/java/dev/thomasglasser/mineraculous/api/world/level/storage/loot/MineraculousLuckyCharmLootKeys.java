package dev.thomasglasser.mineraculous.api.world.level.storage.loot;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class MineraculousLuckyCharmLootKeys {
    // Miraculous
    public static final ResourceKey<LootTable> LADYBUG_MIRACULOUS = miraculous("ladybug");
    public static final ResourceKey<LootTable> CAT_MIRACULOUS = miraculous("cat");
    public static final ResourceKey<LootTable> BUTTERFLY_MIRACULOUS = miraculous("butterfly");

    // Entities
    public static final ResourceKey<LootTable> ENDER_DRAGON = entity("ender_dragon");
    public static final ResourceKey<LootTable> ELDER_GUARDIAN = entity("elder_guardian");
    public static final ResourceKey<LootTable> WARDEN = entity("warden");
    public static final ResourceKey<LootTable> WITHER = entity("wither");

    // Events
    public static final ResourceKey<LootTable> RAID = event("raid");

    private static ResourceKey<LootTable> miraculous(String name) {
        return create("miraculous/" + name);
    }

    private static ResourceKey<LootTable> entity(String name) {
        return create("entity/" + name);
    }

    private static ResourceKey<LootTable> event(String name) {
        return create("event/" + name);
    }

    private static ResourceKey<LootTable> create(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, Mineraculous.modLoc("gameplay/lucky_charm/" + name));
    }
}
