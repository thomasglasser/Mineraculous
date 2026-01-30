package dev.thomasglasser.mineraculous.impl.data.worldgen.features;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;

public class MineraculousTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ALMOND = create("almond");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_ALMOND = create("fancy_almond");

    private static TreeConfiguration.TreeConfigurationBuilder createAlmond() {
        return TreeFeatures.createStraightBlobTree(MineraculousBlocks.ALMOND_WOOD_SET.log().get(), MineraculousBlocks.ALMOND_LEAVES_SET.leaves().get(), 4, 2, 0, 2).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder createFancyAlmond() {
        return (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(MineraculousBlocks.ALMOND_WOOD_SET.log().get()), new FancyTrunkPlacer(3, 11, 0), BlockStateProvider.simple(MineraculousBlocks.ALMOND_LEAVES_SET.leaves().get()), new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4), new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)))).ignoreVines();
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> create(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MineraculousConstants.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, ALMOND, Feature.TREE, createAlmond().build());
        FeatureUtils.register(context, FANCY_ALMOND, Feature.TREE, createFancyAlmond().build());
    }
}
