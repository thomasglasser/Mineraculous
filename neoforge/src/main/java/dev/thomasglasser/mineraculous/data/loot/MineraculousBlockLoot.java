package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.loot.ExtendedBlockLootSubProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class MineraculousBlockLoot extends ExtendedBlockLootSubProvider
{
	protected MineraculousBlockLoot()
	{
		super(Set.of(), FeatureFlags.REGISTRY.allFlags());
	}

	@Override
	protected void generate()
	{
		dropOther(MineraculousBlocks.CATACLYSM_BLOCK.get(), MineraculousItems.CATACLYSM_DUST.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return MineraculousBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toList();
	}
}
