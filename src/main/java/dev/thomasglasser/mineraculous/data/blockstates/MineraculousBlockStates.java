package dev.thomasglasser.mineraculous.data.blockstates;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.blockstates.ExtendedBlockStateProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.SortedMap;

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

		getVariantBuilder(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).get()).forAllStates(blockState ->
		{
			CheeseBlock.Age age = CheeseBlock.Age.FRESH;
			String name = "cheese";
			int bites = blockState.getValue(CheeseBlock.BITES);
			String suffix = bites > 0 ? "_slice" + bites : "";
			String ageName = age.getSerializedName();
			return ConfiguredModel.builder()
					.modelFile(models()
							.withExistingParent(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).getId().getPath() + suffix, modBlockModel("cheese" + suffix + "_base"))
							.texture("inner", modBlockModel("cheese/" + ageName + "_" + name + "_inner"))
							.texture("side", modBlockModel("cheese/" + ageName + "_" + name + "_side"))
							.texture("top", modBlockModel("cheese/" + ageName + "_" + name + "_top"))
							.texture("bottom", modBlockModel("cheese/" + ageName + "_" + name + "_bottom"))
							.texture("particle", modBlockModel("cheese/" + ageName + "_" + name + "_side")))
					.build();
		});

		// TODO: Textures for all
//		cheese(MineraculousBlocks.CHEESE_BLOCKS, "cheese");
//		cheese(MineraculousBlocks.WAXED_CHEESE_BLOCKS, "waxed_cheese");
//		cheese(MineraculousBlocks.CAMEMBERT_BLOCKS, "camembert");
//		cheese(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS, "waxed_camembert");
	}

	protected void cheese(SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> map, String name)
	{
		map.forEach(((age, block) ->
				getVariantBuilder(block.get()).forAllStates(blockState ->
				{
					int bites = blockState.getValue(CheeseBlock.BITES);
					String suffix = bites > 0 ? "_slice" + bites : "";
					String ageName = age.getSerializedName();
					return ConfiguredModel.builder()
							.modelFile(models()
									.withExistingParent(block.getId().getPath() + suffix, modBlockModel("cheese" + suffix + "_base"))
									.texture("inner", modBlockModel("cheese/" + ageName + "_" + name + "_inner"))
									.texture("side", modBlockModel("cheese/" + ageName + "_" + name + "_side"))
									.texture("top", modBlockModel("cheese/" + ageName + "_" + name + "_top"))
									.texture("bottom", modBlockModel("cheese/" + ageName + "_" + name + "_bottom"))
									.texture("particle", modBlockModel("cheese/" + ageName + "_" + name + "_side")))
							.build();
				})
		));
	}
}
