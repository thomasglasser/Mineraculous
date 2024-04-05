package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.tommylib.api.data.loot.ExtendedLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class MineraculousLootTables extends ExtendedLootTableProvider
{

	public MineraculousLootTables(PackOutput pOutput)
	{
		super(pOutput, Set.of(), List.of(
				new SubProviderEntry(MineraculousBlockLoot::new, LootContextParamSets.BLOCK)
		));
	}
}
