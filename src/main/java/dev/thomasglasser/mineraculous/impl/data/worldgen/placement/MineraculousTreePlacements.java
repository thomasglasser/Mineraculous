package dev.thomasglasser.mineraculous.impl.data.worldgen.placement;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousTreeFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MineraculousTreePlacements {
    public static final ResourceKey<PlacedFeature> ALMOND_CHECKED = create("almond_checked");
    public static final ResourceKey<PlacedFeature> ALMOND_CHECKED_002 = create("almond_checked_002");
    public static final ResourceKey<PlacedFeature> FANCY_ALMOND_CHECKED = create("fancy_almond_checked");
    public static final ResourceKey<PlacedFeature> FANCY_ALMOND_CHECKED_002 = create("fancy_almond_checked_002");

    private static ResourceKey<PlacedFeature> create(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MineraculousConstants.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> almondTreeFeature = configuredFeatures.getOrThrow(MineraculousTreeFeatures.ALMOND);
        Holder<ConfiguredFeature<?, ?>> fancyAlmondTreeFeature = configuredFeatures.getOrThrow(MineraculousTreeFeatures.FANCY_ALMOND);

        PlacementUtils.register(context, ALMOND_CHECKED, almondTreeFeature,
                PlacementUtils.filteredByBlockSurvival(MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
        PlacementUtils.register(context, ALMOND_CHECKED_002, almondTreeFeature,
                PlacementUtils.filteredByBlockSurvival(MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
        PlacementUtils.register(context, FANCY_ALMOND_CHECKED, fancyAlmondTreeFeature,
                PlacementUtils.filteredByBlockSurvival(MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
        PlacementUtils.register(context, FANCY_ALMOND_CHECKED_002, fancyAlmondTreeFeature,
                PlacementUtils.filteredByBlockSurvival(MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
    }
}
