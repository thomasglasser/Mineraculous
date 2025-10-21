package dev.thomasglasser.mineraculous.api.world.level.storage.loot.functions;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class MineraculousLootItemFunctionTypes {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<DyeRandomlyFunction>> DYE_RANDOMLY = LOOT_ITEM_FUNCTION_TYPES.register("dye_randomly", () -> new LootItemFunctionType<>(DyeRandomlyFunction.CODEC));

    public static void init() {}
}
