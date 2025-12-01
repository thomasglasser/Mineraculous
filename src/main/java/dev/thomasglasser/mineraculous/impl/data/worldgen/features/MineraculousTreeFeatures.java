package dev.thomasglasser.mineraculous.impl.data.worldgen.features;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MineraculousTreeFeatures extends TreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ALMOND = create("almond");

    private static TreeConfiguration.TreeConfigurationBuilder createAlmond() {
        return createStraightBlobTree(MineraculousBlocks.ALMOND_WOOD_SET.log().get(), MineraculousBlocks.ALMOND_LEAVES_SET.leaves().get(), 4, 2, 0, 2).ignoreVines();
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> create(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MineraculousConstants.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, ALMOND, Feature.TREE, createAlmond().build());
    }
}
