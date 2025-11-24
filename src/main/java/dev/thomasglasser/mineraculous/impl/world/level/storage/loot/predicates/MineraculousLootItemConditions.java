package dev.thomasglasser.mineraculous.impl.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.storage.loot.predicates.HasItem;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousLootItemConditions {
    private static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> HAS_ITEM = register("has_item", HasItem.CODEC);

    private static DeferredHolder<LootItemConditionType, LootItemConditionType> register(String name, MapCodec<? extends LootItemCondition> codec) {
        return LOOT_ITEM_CONDITIONS.register(name, () -> new LootItemConditionType(codec));
    }

    @ApiStatus.Internal
    public static void init() {}
}
