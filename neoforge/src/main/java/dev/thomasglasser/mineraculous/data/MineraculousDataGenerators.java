package dev.thomasglasser.mineraculous.data;

import dev.thomasglasser.mineraculous.data.blockstates.MineraculousBlockStates;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.data.lang.MineraculousEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.data.loot.MineraculousLootTables;
import dev.thomasglasser.mineraculous.data.loot.modifier.MineraculousGlobalLootModifierProvider;
import dev.thomasglasser.mineraculous.data.models.MineraculousItemModels;
import dev.thomasglasser.mineraculous.data.particles.MineraculousParticleDescriptionProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousBlockTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
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
		MineraculousBlockTagsProvider blockTagsProvider = new MineraculousBlockTagsProvider(packOutput, registries, existingFileHelper);
		generator.addProvider(onServer, blockTagsProvider);
		generator.addProvider(onServer, new MineraculousItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));
		generator.addProvider(onServer, new MineraculousCuriosProvider(packOutput, existingFileHelper, registries));
		generator.addProvider(onServer, new MineraculousGlobalLootModifierProvider(packOutput));
		generator.addProvider(onServer, new MineraculousLootTables(packOutput));

		// Client
		generator.addProvider(onClient, new MineraculousBlockStates(packOutput, existingFileHelper));
		generator.addProvider(onClient, new MineraculousItemModels(packOutput, existingFileHelper));
		generator.addProvider(onClient, new MineraculousEnUsLanguageProvider(packOutput));
		generator.addProvider(onClient, new MineraculousParticleDescriptionProvider(packOutput, existingFileHelper));
	}
}
