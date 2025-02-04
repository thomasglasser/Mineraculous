package dev.thomasglasser.mineraculous.world.level.storage.loot.parameters;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class MineraculousLootContextParams {
    public static final LootContextParam<Integer> POWER_LEVEL = create("power_level");
    public static final LootContextParam<ItemStack> SHOOTER = create("shooter");
    public static final LootContextParam<Boolean> HAS_AMMO = create("has_ammo");

    private static <T> LootContextParam<T> create(String id) {
        return new LootContextParam<>(Mineraculous.modLoc(id));
    }
}
