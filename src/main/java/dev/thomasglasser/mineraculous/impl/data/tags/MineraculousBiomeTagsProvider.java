package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBiomeTags;
import dev.thomasglasser.tommylib.api.tags.ConventionalBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousBiomeTagsProvider extends BiomeTagsProvider {
    public MineraculousBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MineraculousBiomeTags.SPAWNS_WARM_VARIANT_BUTTERFLIES)
                .addTag(ConventionalBiomeTags.IS_JUNGLE)
                .addTag(ConventionalBiomeTags.IS_SAVANNA)
                .addTag(ConventionalBiomeTags.IS_SWAMP);

        tag(MineraculousBiomeTags.SPAWNS_COLD_VARIANT_BUTTERFLIES)
                .addTag(ConventionalBiomeTags.IS_SNOWY_PLAINS)
                .addTag(ConventionalBiomeTags.IS_TAIGA);

        tag(MineraculousBiomeTags.SPAWNS_BUTTERFLIES)
                .addTag(MineraculousBiomeTags.SPAWNS_WARM_VARIANT_BUTTERFLIES)
                .addTag(MineraculousBiomeTags.SPAWNS_COLD_VARIANT_BUTTERFLIES)
                .addTag(ConventionalBiomeTags.IS_PLAINS)
                .addTag(ConventionalBiomeTags.IS_FOREST)
                .addTag(ConventionalBiomeTags.IS_FLORAL);
    }
}
