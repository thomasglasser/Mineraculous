package dev.thomasglasser.mineraculous.impl.data.biome;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBiomeTags;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousBiomeModifiers {
    private static ResourceKey<BiomeModifier> create(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, MineraculousConstants.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        addSpawns(context, "spawn_butterflies", biomes.getOrThrow(MineraculousBiomeTags.SPAWNS_BUTTERFLIES), MineraculousEntityTypes.BUTTERFLY, 10, 5, 15);
    }

    private static void addSpawns(BootstrapContext<BiomeModifier> context, String name, HolderSet<Biome> biomes, Holder<EntityType<?>> type, int weight, int minCount, int maxCount) {
        context.register(create(name), BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(biomes, new MobSpawnSettings.SpawnerData(type.value(), weight, minCount, maxCount)));
    }
}
