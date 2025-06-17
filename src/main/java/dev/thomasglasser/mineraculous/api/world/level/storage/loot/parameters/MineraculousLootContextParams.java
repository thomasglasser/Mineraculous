package dev.thomasglasser.mineraculous.api.world.level.storage.loot.parameters;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class MineraculousLootContextParams {
    public static final LootContextParam<Integer> POWER_LEVEL = create("power_level");

    private static <T> LootContextParam<T> create(String name) {
        return new LootContextParam<>(Mineraculous.modLoc(name));
    }
}
