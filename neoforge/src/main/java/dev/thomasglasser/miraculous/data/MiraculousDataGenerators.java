package dev.thomasglasser.miraculous.data;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.data.lang.MiraculousEnUsLanguageProvider;
import dev.thomasglasser.miraculous.data.models.MiraculousItemModels;
import dev.thomasglasser.miraculous.data.tags.MiraculousEntityTypeTagsProvider;
import dev.thomasglasser.miraculous.data.tags.MiraculousItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class MiraculousDataGenerators
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
		generator.addProvider(onServer, new MiraculousEntityTypeTagsProvider(packOutput, registries, existingFileHelper));
		BlockTagsProvider blockTagsProvider = new BlockTagsProvider(packOutput, registries, Miraculous.MOD_ID, existingFileHelper)
		{
			@Override
			protected void addTags(HolderLookup.Provider pProvider)
			{}
		};
		generator.addProvider(onServer, blockTagsProvider);
		generator.addProvider(onServer, new MiraculousItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));

		// Client
		generator.addProvider(onClient, new MiraculousItemModels(packOutput, existingFileHelper));
		generator.addProvider(onClient, new MiraculousEnUsLanguageProvider(packOutput));
	}
}
