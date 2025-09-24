package dev.thomasglasser.mineraculous.impl.data;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.data.advancements.MineraculousAdvancementProvider;
import dev.thomasglasser.mineraculous.impl.data.blockstates.MineraculousBlockStateProvider;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.impl.data.datamaps.MineraculousDataMapProvider;
import dev.thomasglasser.mineraculous.impl.data.lang.MineraculousEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.impl.data.lang.expansions.AkumatizationPackEnUsLanguageProvider;
import dev.thomasglasser.mineraculous.impl.data.loot.MineraculousLootTables;
import dev.thomasglasser.mineraculous.impl.data.models.MineraculousItemModelProvider;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.MineraculousBookProvider;
import dev.thomasglasser.mineraculous.impl.data.particles.MineraculousParticleDescriptionProvider;
import dev.thomasglasser.mineraculous.impl.data.recipes.MineraculousRecipeProvider;
import dev.thomasglasser.mineraculous.impl.data.sounds.MineraculousSoundDefinitionsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MineraculousBlockTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MineraculousDamageTypeTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MineraculousItemTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MineraculousPaintingVariantTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MineraculousPoiTypeTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.MiraculousTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.tags.client.MineraculousResourceLocationClientTagsProvider;
import dev.thomasglasser.mineraculous.impl.data.worldgen.MineraculousWorldgenModifiers;
import dev.thomasglasser.mineraculous.impl.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.mineraculous.impl.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.tommylib.api.data.DataGenerationUtils;
import dev.worldgen.lithostitched.registry.LithostitchedRegistryKeys;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
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
            .add(MineraculousRegistries.ABILITY, Abilities::bootstrap)
            .add(MineraculousRegistries.MIRACULOUS, Miraculouses::bootstrap)
            .add(MineraculousRegistries.KAMIKOTIZATION, context -> {
                // TODO: Remove when testing is done (don't forget to remove the assets)
                HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);
                HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("cat")),
                        new Kamikotization(
                                "Kitty",
                                ItemPredicate.Builder.item().build(),
                                Either.right(abilities.getOrThrow(Abilities.CATACLYSM)),
                                HolderSet.direct(abilities.getOrThrow(Abilities.CAT_VISION))));
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("ladybug")),
                        new Kamikotization(
                                "Bugaboo",
                                ItemPredicate.Builder.item().build(),
                                Either.left(Items.DIAMOND_SWORD.getDefaultInstance()),
                                HolderSet.direct(abilities.getOrThrow(Abilities.CAT_VISION), abilities.getOrThrow(Abilities.KAMIKO_CONTROL))));
                ItemStack stormyTool = Items.DIAMOND.getDefaultInstance();
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
                stormyTool.enchant(enchantments.getOrThrow(Enchantments.KNOCKBACK), 10);
                context.register(ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, Mineraculous.modLoc("stormy")),
                        new Kamikotization(
                                "Stormy Tester",
                                ItemPredicate.Builder.item().of(ItemTags.BANNERS).build(),
                                Either.left(stormyTool),
                                HolderSet.empty()));
            });

    public static void onGatherData(GatherDataEvent event) {
        // Server
        event.createDatapackRegistryObjects(BUILDER);
        DataGenerationUtils.createRegistryDumpReport(event, Mineraculous.MOD_ID);
        event.createProvider(MineraculousLootTables::new);
        event.createProvider(MineraculousRecipeProvider::new);
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

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        generateAkumatizationPack(generator, packOutput);
    }

    private static void generateAkumatizationPack(DataGenerator generator, PackOutput packOutput) {
        packOutput = MineraculousPacks.AKUMATIZATION.toSubPackOutput(packOutput);
        generator.addProvider(true, MineraculousPacks.AKUMATIZATION.toGenerator(packOutput));

        // Client
        generator.addProvider(true, new AkumatizationPackEnUsLanguageProvider(packOutput));
    }
}
