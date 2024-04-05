package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedBlockTagsProvider;
import dev.thomasglasser.tommylib.api.tags.TommyLibBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MineraculousBlockTagsProvider extends ExtendedBlockTagsProvider
{
	public MineraculousBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, Mineraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider pProvider)
	{
		tag(MineraculousBlockTags.CATACLYSM_IMMUNE)
				.add(Blocks.WATER)
				.add(Blocks.LAVA)
				.add(Blocks.FIRE)
				.add(Blocks.SOUL_FIRE)
				.add(MineraculousBlocks.CATACLYSM_BLOCK.get())
				.addOptionalTag(TommyLibBlockTags.UNBREAKABLE);
	}
}
