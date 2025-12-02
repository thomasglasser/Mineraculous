package dev.thomasglasser.mineraculous.impl.data.worldgen.placement;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousVegetationFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MineraculousVegetationPlacement {
    public static final ResourceKey<PlacedFeature> COMMON_TREES_ALMONDS = create("common_trees_almonds");
    public static final ResourceKey<PlacedFeature> RARE_TREES_ALMONDS = create("rare_trees_almonds");

    public static ResourceKey<PlacedFeature> create(String key) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MineraculousConstants.modLoc(key));
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> holder1 = configuredFeatures.getOrThrow(MineraculousVegetationFeatures.TREES_ALMONDS);
        Holder<ConfiguredFeature<?, ?>> holder2 = configuredFeatures.getOrThrow(MineraculousVegetationFeatures.TREES_ALMONDS_002);
        PlacementUtils.register(context, RARE_TREES_ALMONDS, holder1, VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(context, COMMON_TREES_ALMONDS, holder2, VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
    }
}
