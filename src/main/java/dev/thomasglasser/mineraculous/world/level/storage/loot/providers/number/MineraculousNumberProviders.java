package dev.thomasglasser.mineraculous.world.level.storage.loot.providers.number;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class MineraculousNumberProviders {
    private static final DeferredRegister<LootNumberProviderType> NUMBER_PROVIDERS = DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<LootNumberProviderType, LootNumberProviderType> POWER_LEVEL = register("power_level_multiplier", PowerLevelMultiplierGenerator.CODEC);

    private static DeferredHolder<LootNumberProviderType, LootNumberProviderType> register(String name, MapCodec<? extends NumberProvider> codec) {
        return NUMBER_PROVIDERS.register(name, () -> new LootNumberProviderType(codec));
    }

    public static void init() {}
}
