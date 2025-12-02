package dev.thomasglasser.mineraculous.impl.data.worldgen.features;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.data.worldgen.placement.MineraculousTreePlacements;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MineraculousVegetationFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_ALMONDS = create("trees_almonds");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_ALMONDS_002 = create("trees_almonds_002");

    public static ResourceKey<ConfiguredFeature<?, ?>> create(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MineraculousConstants.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> almond = placedFeatures.getOrThrow(MineraculousTreePlacements.ALMOND_CHECKED);
        Holder<PlacedFeature> fancyAlmond = placedFeatures.getOrThrow(MineraculousTreePlacements.FANCY_ALMOND_CHECKED);
        Holder<PlacedFeature> almondRare = placedFeatures.getOrThrow(MineraculousTreePlacements.ALMOND_CHECKED_002);
        Holder<PlacedFeature> fancyAlmondRare = placedFeatures.getOrThrow(MineraculousTreePlacements.FANCY_ALMOND_CHECKED_002);

        FeatureUtils.register(
                context, TREES_ALMONDS, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(almond, 0.8F)), fancyAlmond));
        FeatureUtils.register(
                context, TREES_ALMONDS_002, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(almondRare, 0.8F)), fancyAlmondRare));
    }
}
