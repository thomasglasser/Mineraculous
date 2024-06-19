package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.tommylib.api.data.loot.ExtendedLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MineraculousLootTables extends ExtendedLootTableProvider
{

	public MineraculousLootTables(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> lookupProvider)
	{
		super(pOutput, Set.of(), List.of(
				new SubProviderEntry(MineraculousBlockLoot::new, LootContextParamSets.BLOCK)
		), lookupProvider);
	}
}
