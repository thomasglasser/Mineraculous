package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousEntityTypeTags;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedIntrinsicHolderTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousEntityTypeTagsProvider extends ExtendedIntrinsicHolderTagsProvider<EntityType<?>> {
    public MineraculousEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENTITY_TYPE, provider, type -> type.builtInRegistryHolder().key(), MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MineraculousEntityTypeTags.CATACLYSM_IMMUNE)
                .add(MineraculousEntityTypes.KWAMI.get());

        tag(MineraculousEntityTypeTags.BUTTERFLIES)
                .add(MineraculousEntityTypes.BUTTERFLY.get());

        tag(MineraculousEntityTypeTags.LADYBUG_YOYO_EXTENDED_RANGE)
                .add(MineraculousEntityTypes.KAMIKO.get());
    }
}
