package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class MineraculousBiomeTags {
    public static final TagKey<Biome> SPAWNS_BUTTERFLIES = create("spawns_butterflies");
    public static final TagKey<Biome> SPAWNS_COLD_VARIANT_BUTTERFLIES = create("spawns_cold_variant_butterflies");
    public static final TagKey<Biome> SPAWNS_WARM_VARIANT_BUTTERFLIES = create("spawns_warm_variant_butterflies");

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, MineraculousConstants.modLoc(name));
    }
}
