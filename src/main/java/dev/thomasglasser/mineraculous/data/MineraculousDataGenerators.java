package dev.thomasglasser.mineraculous.data;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.data.advancements.MineraculousAdvancementProvider;
import dev.thomasglasser.mineraculous.data.blockstates.MineraculousBlockStateProvider;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.data.datamaps.MineraculousDataMapProvider;
import dev.thomasglasser.mineraculous.data.lang.MineraculousEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.data.lang.expansions.AkumatizationPackEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.data.loot.MineraculousLootTables;
import dev.thomasglasser.mineraculous.data.models.MineraculousItemModelProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.MineraculousBookProvider;
import dev.thomasglasser.mineraculous.data.particles.MineraculousParticleDescriptionProvider;
import dev.thomasglasser.mineraculous.data.recipes.MineraculousRecipes;
import dev.thomasglasser.mineraculous.data.sounds.MineraculousSoundDefinitionsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousBlockTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousDamageTypeTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousItemTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousPaintingVariantTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MineraculousPoiTypeTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.MiraculousTagProvider;
import dev.thomasglasser.mineraculous.data.trimmed.MineraculousTrimDatagenSuite;
import dev.thomasglasser.mineraculous.data.worldgen.MineraculousWorldgenModifiers;
import dev.thomasglasser.mineraculous.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.tommylib.api.data.info.ModRegistryDumpReport;
import dev.worldgen.lithostitched.registry.LithostitchedRegistryKeys;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MineraculousDataGenerators {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, MineraculousDamageTypes::bootstrap)
            .add(Registries.PAINTING_VARIANT, MineraculousPaintingVariants::bootstrap)
            .add(LithostitchedRegistryKeys.WORLDGEN_MODIFIER, MineraculousWorldgenModifiers::bootstrap)
            .add(MineraculousRegistries.ABILITY, MineraculousAbilities::bootstrap)
            .add(MineraculousRegistries.MIRACULOUS, MineraculousMiraculous::bootstrap)
            .add(MineraculousRegistries.KAMIKOTIZATION, context -> {
                // TODO: Remove when testing is done (don't forget to remove the assets)
                HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);

                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("cat")),
                        new Kamikotization(
                                "Kitty",
                                ItemPredicate.Builder.item().build(),
                                Optional.of(abilities.getOrThrow(MineraculousAbilities.CATACLYSM)),
                                List.of(abilities.getOrThrow(MineraculousAbilities.CAT_VISION))));
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("ladybug")),
                        new Kamikotization(
                                "Bugaboo",
                                ItemPredicate.Builder.item().build(),
                                Optional.empty(),
                                List.of(abilities.getOrThrow(MineraculousAbilities.CAT_VISION), abilities.getOrThrow(MineraculousAbilities.KAMIKOTIZED_COMMUNICATION))));
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("butterfly")),
                        new Kamikotization(
                                "Betterfly",
                                ItemPredicate.Builder.item().build(),
                                Optional.empty(),
                                List.of()));
            });

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        boolean onServer = event.includeServer();
        boolean onClient = event.includeClient();

        MineraculousEnUsLanguageProvider enUs = new MineraculousEnUsLanguageProvider(packOutput);

        // Trims
        new MineraculousTrimDatagenSuite(event, enUs);

        // Server
        DatapackBuiltinEntriesProvider builtinEntriesProvider = new DatapackBuiltinEntriesProvider(packOutput, registries, BUILDER, Set.of(Mineraculous.MOD_ID));
        generator.addProvider(onServer, builtinEntriesProvider);
        registries = builtinEntriesProvider.getRegistryProvider();
        generator.addProvider(onServer, new ModRegistryDumpReport(packOutput, Mineraculous.MOD_ID, registries));
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
        generator.addProvider(onServer, new MineraculousPaintingVariantTagsProvider(packOutput, registries, existingFileHelper));
        generator.addProvider(onServer, new MiraculousTagProvider(packOutput, registries, Mineraculous.MOD_ID, existingFileHelper));
        generator.addProvider(onServer, new MineraculousBookProvider(packOutput, registries, enUs::add));

        // Client
        generator.addProvider(onClient, new MineraculousBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(onClient, new MineraculousItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(onClient, new MineraculousParticleDescriptionProvider(packOutput, existingFileHelper));
        generator.addProvider(onClient, new MineraculousSoundDefinitionsProvider(packOutput, existingFileHelper));
        generator.addProvider(onClient, enUs);

        generateAkumatizationPack(generator, new PackOutput(packOutput.getOutputFolder().resolve("packs/" + MineraculousPacks.AKUMATIZATION.knownPack().namespace() + "/" + MineraculousPacks.AKUMATIZATION.knownPack().id())), registries, existingFileHelper, onServer, onClient);
    }

    private static void generateAkumatizationPack(DataGenerator generator, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper, boolean onServer, boolean onClient) {
        // Client
        generator.addProvider(onClient, new AkumatizationPackEnUsLanguageProvider(packOutput));
    }
}
