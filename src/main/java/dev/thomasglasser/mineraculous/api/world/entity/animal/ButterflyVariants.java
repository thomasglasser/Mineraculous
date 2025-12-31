package dev.thomasglasser.mineraculous.api.world.entity.animal;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.ApiStatus;

public class ButterflyVariants {
    /// The default, a white butterfly.
    public static final ResourceKey<ButterflyVariant> TEMPERATE = create("temperate");
    /// Doesn't spawn naturally, only obtainable via kamikotization.
    public static final ResourceKey<ButterflyVariant> KAMIKO = create("kamiko");
    /// Found in warm biomes with adequate vegetation, based on the Monarch butterfly.
    public static final ResourceKey<ButterflyVariant> WARM = create("warm");
    /// Found in cold biomes with adequate vegetation, based on the Apollo butterfly.
    public static final ResourceKey<ButterflyVariant> COLD = create("cold");

    private static ResourceKey<ButterflyVariant> create(String name) {
        return ResourceKey.create(MineraculousRegistries.BUTTERFLY_VARIANT, MineraculousConstants.modLoc(name));
    }

    @ApiStatus.Internal
    public static Holder<ButterflyVariant> getSpawnVariant(RegistryAccess registryAccess, Holder<Biome> biome) {
        Registry<ButterflyVariant> registry = registryAccess.registryOrThrow(MineraculousRegistries.BUTTERFLY_VARIANT);
        return registry.holders()
                .filter(variant -> variant.value().biomes().contains(biome))
                .findFirst()
                .or(() -> registry.getHolder(TEMPERATE))
                .or(registry::getAny)
                .orElseThrow();
    }

    @ApiStatus.Internal
    public static void bootstrap(BootstrapContext<ButterflyVariant> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        register(context, TEMPERATE, HolderSet.empty());
        register(context, KAMIKO, HolderSet.empty());
        register(context, WARM, biomes.getOrThrow(MineraculousBiomeTags.SPAWNS_WARM_VARIANT_BUTTERFLIES));
        register(context, COLD, biomes.getOrThrow(MineraculousBiomeTags.SPAWNS_COLD_VARIANT_BUTTERFLIES));
    }

    /**
     * Registers the provided variant to the provided context for the provided biomes.
     * 
     * @param context The context to register the variant to
     * @param variant The variant to register
     * @param biomes  The biomes the variant should spawn in
     */
    public static void register(BootstrapContext<ButterflyVariant> context, ResourceKey<ButterflyVariant> variant, HolderSet<Biome> biomes) {
        ResourceLocation textureLoc = variant.location().withPath(path -> "entity/butterfly/" + path);
        context.register(variant, new ButterflyVariant(textureLoc, biomes));
    }
}
