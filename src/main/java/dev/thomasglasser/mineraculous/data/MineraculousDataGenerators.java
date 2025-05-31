package dev.thomasglasser.mineraculous.data;

import com.mojang.datafixers.util.Either;
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
import dev.thomasglasser.mineraculous.data.tags.MiraculousTagsProvider;
import dev.thomasglasser.mineraculous.data.tags.client.MineraculousResourceLocationClientTagsProvider;
import dev.thomasglasser.mineraculous.data.trimmed.MineraculousTrimDatagenSuite;
import dev.thomasglasser.mineraculous.data.worldgen.MineraculousWorldgenModifiers;
import dev.thomasglasser.mineraculous.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.tommylib.api.data.DataGenerationUtils;
import dev.worldgen.lithostitched.registry.LithostitchedRegistryKeys;
import java.util.List;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MineraculousDataGenerators {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, MineraculousDamageTypes::bootstrap)
            .add(Registries.PAINTING_VARIANT, MineraculousPaintingVariants::bootstrap)
            .add(Registries.TRIM_PATTERN, MineraculousTrimPatterns::bootstrap)
            .add(LithostitchedRegistryKeys.WORLDGEN_MODIFIER, MineraculousWorldgenModifiers::bootstrap)
            .add(MineraculousRegistries.ABILITY, MineraculousAbilities::bootstrap)
            .add(MineraculousRegistries.MIRACULOUS, Miraculouses::bootstrap)
            .add(MineraculousRegistries.KAMIKOTIZATION, context -> {
                // TODO: Remove when testing is done (don't forget to remove the assets)
                HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);
                HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("cat")),
                        new Kamikotization(
                                "Kitty",
                                ItemPredicate.Builder.item().build(),
                                Either.right(abilities.getOrThrow(MineraculousAbilities.CATACLYSM)),
                                List.of(abilities.getOrThrow(MineraculousAbilities.CAT_VISION))));
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("ladybug")),
                        new Kamikotization(
                                "Bugaboo",
                                ItemPredicate.Builder.item().build(),
                                Either.left(Items.DIAMOND_SWORD.getDefaultInstance()),
                                List.of(abilities.getOrThrow(MineraculousAbilities.CAT_VISION), abilities.getOrThrow(MineraculousAbilities.KAMIKOTIZED_COMMUNICATION))));
                ItemStack stormyTool = Items.DIAMOND.getDefaultInstance();
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.KNOCKBACK), 10);
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("stormy")),
                        new Kamikotization(
                                "Stormy Tester",
                                ItemPredicate.Builder.item().of(ItemTags.BANNERS).build(),
                                Either.left(stormyTool),
                                List.of()));
            });

    public static void onGatherData(GatherDataEvent event) {
        // Server
        event.createDatapackRegistryObjects(BUILDER);
        DataGenerationUtils.createRegistryDumpReport(event, Mineraculous.MOD_ID);
        event.createProvider(MineraculousLootTables::new);
        event.createProvider(MineraculousRecipes::new);
        DataGenerationUtils.createBlockAndItemTags(event, MineraculousBlockTagsProvider::new, MineraculousItemTagsProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousPoiTypeTagsProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousDamageTypeTagsProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousPaintingVariantTagsProvider::new);
        DataGenerationUtils.createProvider(event, MiraculousTagsProvider::new);
        event.createProvider(MineraculousDataMapProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousCuriosProvider::new);

        // Common
        DataGenerationUtils.createLangDependent(event, MineraculousEnUsLanguageProvider::new, MineraculousAdvancementProvider::new, MineraculousBookProvider::new);

        // Client
        DataGenerationUtils.createProvider(event, MineraculousBlockStateProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousItemModelProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousParticleDescriptionProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousSoundDefinitionsProvider::new);
        DataGenerationUtils.createProvider(event, MineraculousResourceLocationClientTagsProvider::new);

        generateAkumatizationPack(event.getGenerator(), new PackOutput(event.getGenerator().getPackOutput().getOutputFolder().resolve(MineraculousPacks.AKUMATIZATION.path())));
    }

    private static void generateAkumatizationPack(DataGenerator generator, PackOutput packOutput) {
        // Client
        generator.addProvider(true, new AkumatizationPackEnUsLanguageProvider(packOutput));
    }
}
