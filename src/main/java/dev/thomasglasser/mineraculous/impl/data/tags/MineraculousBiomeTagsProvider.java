package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousBiomeTagsProvider extends BiomeTagsProvider {
    public MineraculousBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MineraculousBiomeTags.HAS_COMMON_ALMOND_TREES)
                .add(Biomes.WOODED_BADLANDS);
        tag(MineraculousBiomeTags.HAS_RARE_ALMOND_TREES)
                .addTags(BiomeTags.IS_SAVANNA);
    }
}
