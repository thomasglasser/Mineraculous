package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class MineraculousBiomeTags {
    /// Common Almonds Trees Biomes
    public static final TagKey<Biome> HAS_COMMON_ALMOND_TREES = create("has_common_almond_trees");
    /// Rare Almonds Trees Biomes
    public static final TagKey<Biome> HAS_RARE_ALMOND_TREES = create("has_rare_almond_trees");

    private MineraculousBiomeTags() {}

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, MineraculousConstants.modLoc(name));
    }
}
