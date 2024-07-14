package dev.thomasglasser.mineraculous.data;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.data.advancements.MineraculousAdvancementProvider;
import dev.thomasglasser.mineraculous.data.blockstates.MineraculousBlockStates;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.data.datamaps.MineraculousDataMapProvider;
import dev.thomasglasser.mineraculous.data.lang.MineraculousEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.data.loot.MineraculousLootTables;
import dev.thomasglasser.mineraculous.data.models.MineraculousItemModels;
import dev.thomasglasser.mineraculous.data.particles.MineraculousParticleDescriptionProvider;
import dev.thomasglasser.mineraculous.data.recipes.MineraculousRecipes;
import dev.thomasglasser.mineraculous.data.tags.MineraculousBlockTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousDamageTypeTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousItemTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousPoiTypeTagsProvider;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MineraculousDataGenerators {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, MineraculousDamageTypes::bootstrap);

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        boolean onServer = event.includeServer();
        boolean onClient = event.includeClient();

        MineraculousEnUsLanguageProvider enUs = new MineraculousEnUsLanguageProvider(packOutput);

        // Server
        DatapackBuiltinEntriesProvider builtinEntriesProvider = new DatapackBuiltinEntriesProvider(packOutput, registries, BUILDER, Set.of(Mineraculous.MOD_ID));
        generator.addProvider(onServer, builtinEntriesProvider);
        registries = builtinEntriesProvider.getRegistryProvider();
        MineraculousBlockTagsProvider blockTagsProvider = new MineraculousBlockTagsProvider(packOutput, registries, existingFileHelper);
        generator.addProvider(onServer, blockTagsProvider);
        generator.addProvider(onServer, new MineraculousItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(onServer, new MineraculousCuriosProvider(packOutput, existingFileHelper, registries));
        generator.addProvider(onServer, new MineraculousLootTables(packOutput, registries));
        generator.addProvider(onServer, new MineraculousRecipes(packOutput, registries));
        generator.addProvider(onServer, new MineraculousPoiTypeTagsProvider(packOutput, registries, existingFileHelper));
        generator.addProvider(onServer, new MineraculousDataMapProvider(packOutput, registries));
        generator.addProvider(onServer, new MineraculousAdvancementProvider(packOutput, registries, existingFileHelper, enUs));
        generator.addProvider(onServer, new MineraculousDamageTypeTagsProvider(packOutput, registries, existingFileHelper));

        // Client
        generator.addProvider(onClient, new MineraculousBlockStates(packOutput, existingFileHelper));
        generator.addProvider(onClient, new MineraculousItemModels(packOutput, existingFileHelper));
        generator.addProvider(onClient, new MineraculousParticleDescriptionProvider(packOutput, existingFileHelper));
        generator.addProvider(onClient, enUs);
    }
}
