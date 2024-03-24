package dev.thomasglasser.mineraculous.data;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.data.lang.MineraculousEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.data.models.MineraculousItemModels;
import dev.thomasglasser.mineraculous.data.tags.MineraculousItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class MineraculousDataGenerators
{
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		boolean onServer = event.includeServer();
		boolean onClient = event.includeClient();

		// Server
		BlockTagsProvider blockTagsProvider = new BlockTagsProvider(packOutput, registries, Mineraculous.MOD_ID, existingFileHelper)
		{
			@Override
			protected void addTags(HolderLookup.Provider pProvider)
			{}
		};
		generator.addProvider(onServer, blockTagsProvider);
		generator.addProvider(onServer, new MineraculousItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));
		generator.addProvider(onServer, new MineraculousCuriosProvider(packOutput, existingFileHelper, registries));

		// Client
		generator.addProvider(onClient, new MineraculousItemModels(packOutput, existingFileHelper));
		generator.addProvider(onClient, new MineraculousEnUsLanguageProvider(packOutput));
	}
}
