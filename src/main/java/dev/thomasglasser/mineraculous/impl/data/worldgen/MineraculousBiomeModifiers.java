package dev.thomasglasser.mineraculous.impl.data.worldgen;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBiomeTags;
import dev.thomasglasser.mineraculous.impl.data.worldgen.placement.MineraculousTreePlacements;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_COMMON_ALMOND_TREES = registerKey("add_common_almond_trees");
    public static final ResourceKey<BiomeModifier> ADD_RARE_ALMOND_TREES = registerKey("add_rare_almond_trees");


    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        // CF -> PF -> BM
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ADD_COMMON_ALMOND_TREES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(MineraculousBiomeTags.HAS_COMMON_ALMOND_TREES),
                HolderSet.direct(placedFeatures.getOrThrow(MineraculousTreePlacements.COMMON_ALMOND_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_RARE_ALMOND_TREES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(MineraculousBiomeTags.HAS_RARE_ALMOND_TREES),
                HolderSet.direct(placedFeatures.getOrThrow(MineraculousTreePlacements.COMMON_ALMOND_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(MineraculousConstants.MOD_ID, name));
    }
}
