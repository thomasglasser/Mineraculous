package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.entity.ai.village.poi.MineraculousPoiTypes;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousPoiTypeTagsProvider extends ExtendedTagsProvider<PoiType> {
    public MineraculousPoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, lookupProvider, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE)
                .add(MineraculousPoiTypes.FROMAGER);
    }
}
