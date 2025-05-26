package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MiraculousTagProvider extends ExtendedTagsProvider<Miraculous> {
    public MiraculousTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, MineraculousRegistries.MIRACULOUS, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MineraculousMiraculousTags.CAN_USE_BUTTERFLY_CANE)
                .add(Miraculouses.BUTTERFLY);

        tag(MineraculousMiraculousTags.CAN_USE_CAT_STAFF)
                .add(Miraculouses.CAT);

        tag(MineraculousMiraculousTags.CAN_USE_LADYBUG_YOYO)
                .add(Miraculouses.LADYBUG);
    }
}
