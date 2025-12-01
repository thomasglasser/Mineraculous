package dev.thomasglasser.mineraculous.impl.data.worldgen.placement;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousTreeFeatures;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class MineraculousTreePlacements {
    public static final ResourceKey<PlacedFeature> ALMOND_CHECKED = create("almond_checked");
    public static final ResourceKey<PlacedFeature> ALMOND_CHECKED_002 = create("almond_checked_002");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        create(context, ALMOND_CHECKED, configuredFeatures.getOrThrow(MineraculousTreeFeatures.ALMOND),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
        create(context, ALMOND_CHECKED_002, configuredFeatures.getOrThrow(MineraculousTreeFeatures.ALMOND),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), MineraculousBlocks.ALMOND_LEAVES_SET.sapling().get()));
    }

    private static ResourceKey<PlacedFeature> create(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MineraculousConstants.modLoc(name));
    }

    private static void create(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
