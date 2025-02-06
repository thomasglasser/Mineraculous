package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParamSets;
import dev.thomasglasser.tommylib.api.data.loot.ExtendedLootTableProvider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class MineraculousLootTables extends ExtendedLootTableProvider {
    public MineraculousLootTables(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(pOutput, Set.of(), List.of(
                new SubProviderEntry(MineraculousBlockLoot::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(MineraculousGiftLoot::new, LootContextParamSets.GIFT),
                new SubProviderEntry(MineraculousLuckyCharmLoot::new, MineraculousLootContextParamSets.LUCKY_CHARM)), lookupProvider);
    }
}
