package dev.thomasglasser.mineraculous.data.blockstates;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.blockstates.ExtendedBlockStateProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousBlockStates extends ExtendedBlockStateProvider
{

	public MineraculousBlockStates(PackOutput output, ExistingFileHelper exFileHelper)
	{
		super(output, Mineraculous.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		simpleBlock(MineraculousBlocks.CATACLYSM_BLOCK.get());
		for (CheeseBlock.Age age: CheeseBlock.Age.values()) {
			simpleBlock(MineraculousBlocks.CHEESE_BLOCK.get(age).get());
			simpleBlock(MineraculousBlocks.WAXED_CHEESE_BLOCK.get(age).get());
			simpleBlock(MineraculousBlocks.CAMEMBERT_BLOCK.get(age).get());
			simpleBlock(MineraculousBlocks.WAXED_CAMEMBERT_BLOCK.get(age).get());
		}
	}
}
