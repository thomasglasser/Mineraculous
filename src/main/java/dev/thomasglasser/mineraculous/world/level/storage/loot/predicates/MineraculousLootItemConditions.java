package dev.thomasglasser.mineraculous.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class MineraculousLootItemConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> HAS_ITEM = register("has_item", HasItem.CODEC);

    private static DeferredHolder<LootItemConditionType, LootItemConditionType> register(String name, MapCodec<? extends LootItemCondition> codec) {
        return LOOT_ITEM_CONDITIONS.register(name, () -> new LootItemConditionType(codec));
    }

    public static void init() {}
}
