package dev.thomasglasser.mineraculous.impl.data.worldgen.placement;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousTreeFeatures;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class MineraculousTreePlacements {
    public static final ResourceKey<PlacedFeature> COMMON_ALMOND_PLACED_KEY = registerKey("common_almond_placed");
    public static final ResourceKey<PlacedFeature> RARE_ALMOND_PLACED_KEY = registerKey("rare_almond_placed");


    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatureLookup = context.lookup(Registries.CONFIGURED_FEATURE);
        register(context, COMMON_ALMOND_PLACED_KEY, configuredFeatureLookup.getOrThrow(MineraculousTreeFeatures.ALMOND),
                VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(67), MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
        register(context, RARE_ALMOND_PLACED_KEY, configuredFeatureLookup.getOrThrow(MineraculousTreeFeatures.ALMOND),
                VegetationPlacements.treePlacement(RarityFilter.onAverageOnceEvery(420), MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MineraculousConstants.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
