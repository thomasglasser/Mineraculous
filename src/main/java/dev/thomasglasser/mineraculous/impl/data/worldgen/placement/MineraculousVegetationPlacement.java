package dev.thomasglasser.mineraculous.impl.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousVegetationFeatures;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class MineraculousVegetationPlacement {
    public static final ResourceKey<PlacedFeature> COMMON_TREES_ALMONDS = create("common_trees_almonds");
    public static final ResourceKey<PlacedFeature> RARE_TREES_ALMONDS = create("rare_trees_almonds");
    private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

    public static ResourceKey<PlacedFeature> create(String key) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MineraculousConstants.modLoc(key));
    }

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase(PlacementModifier placement) {
        return ImmutableList.<PlacementModifier>builder()
                .add(placement)
                .add(InSquarePlacement.spread())
                .add(TREE_THRESHOLD)
                .add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
                .add(BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlacement(PlacementModifier placement) {
        return treePlacementBase(placement).build();
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> holder1 = configuredFeatures.getOrThrow(MineraculousVegetationFeatures.TREES_ALMONDS);
        Holder<ConfiguredFeature<?, ?>> holder2 = configuredFeatures.getOrThrow(MineraculousVegetationFeatures.TREES_ALMONDS_002);
        PlacementUtils.register(context, RARE_TREES_ALMONDS, holder1, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
        PlacementUtils.register(context, COMMON_TREES_ALMONDS, holder2, treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
    }
}
