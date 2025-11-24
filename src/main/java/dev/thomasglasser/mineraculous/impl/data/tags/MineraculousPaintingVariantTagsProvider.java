package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedTagsProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousPaintingVariantTagsProvider extends ExtendedTagsProvider<PaintingVariant> {
    public MineraculousPaintingVariantTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.PAINTING_VARIANT, provider, MineraculousConstants.MOD_ID, existingFileHelper);
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
