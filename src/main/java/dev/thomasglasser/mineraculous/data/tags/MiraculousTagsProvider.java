package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MiraculousTagsProvider extends ExtendedTagsProvider<Miraculous> {
    public MiraculousTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        this(output, lookupProvider, Mineraculous.MOD_ID, existingFileHelper);
    }

    protected MiraculousTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, MineraculousRegistries.MIRACULOUS, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MiraculousTags.CAN_USE_BUTTERFLY_CANE)
                .add(Miraculouses.BUTTERFLY);

        tag(MiraculousTags.CAN_USE_CAT_STAFF)
                .add(Miraculouses.CAT);

        tag(MiraculousTags.CAN_USE_LADYBUG_YOYO)
                .add(Miraculouses.LADYBUG);
    }
}
