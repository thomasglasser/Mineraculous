package dev.thomasglasser.mineraculous.impl.world.level.storage.loot;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class MineraculousGiftLootKeys {
    public static final ResourceKey<LootTable> FROMAGER_GIFT = create("fromager_gift");
    public static final ResourceKey<LootTable> BAKER_GIFT = create("baker_gift");

    private static ResourceKey<LootTable> create(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, MineraculousConstants.modLoc("gameplay/hero_of_the_village/" + name));
    }
}
