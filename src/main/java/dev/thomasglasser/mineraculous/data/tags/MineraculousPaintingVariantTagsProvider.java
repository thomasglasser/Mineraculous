package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.decoration.MineraculousPaintingVariants;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.tags.PaintingVariantTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousPaintingVariantTagsProvider extends PaintingVariantTagsProvider {
    public MineraculousPaintingVariantTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PaintingVariantTags.PLACEABLE)
                .add(MineraculousPaintingVariants.LADYBUG)
                .add(MineraculousPaintingVariants.MINI_LADYBUG)
                .add(MineraculousPaintingVariants.CAT)
                .add(MineraculousPaintingVariants.MINI_CAT)
                .add(MineraculousPaintingVariants.BUTTERFLY)
                .add(MineraculousPaintingVariants.MINI_BUTTERFLY);
    }
}
