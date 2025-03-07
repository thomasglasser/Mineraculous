package dev.thomasglasser.mineraculous.world.level.storage.loot;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class MineraculousLuckyCharmLootKeys {
    // Miraculous
    public static final ResourceKey<LootTable> CAT_MIRACULOUS = miraculous("cat");

    // Entities
    public static final ResourceKey<LootTable> ENDER_DRAGON = entity("ender_dragon");
    public static final ResourceKey<LootTable> ELDER_GUARDIAN = entity("elder_guardian");
    public static final ResourceKey<LootTable> WARDEN = entity("warden");
    public static final ResourceKey<LootTable> WITHER = entity("wither");
    public static final ResourceKey<LootTable> RAID = entity("raid");

    private static ResourceKey<LootTable> miraculous(String name) {
        return modLoc("miraculous/" + name);
    }

    private static ResourceKey<LootTable> entity(String name) {
        return modLoc("entity/" + name);
    }

    private static ResourceKey<LootTable> modLoc(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, Mineraculous.modLoc("gameplay/lucky_charm/" + name));
    }
}
