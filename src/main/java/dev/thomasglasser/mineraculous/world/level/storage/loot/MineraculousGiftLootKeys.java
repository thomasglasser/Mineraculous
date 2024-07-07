package dev.thomasglasser.mineraculous.world.level.storage.loot;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class MineraculousGiftLootKeys {
    public static final ResourceKey<LootTable> FROMAGER_GIFT = modLoc("fromager_gift");

    private static ResourceKey<LootTable> modLoc(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, Mineraculous.modLoc("gameplay/hero_of_the_village/" + name));
    }
}
