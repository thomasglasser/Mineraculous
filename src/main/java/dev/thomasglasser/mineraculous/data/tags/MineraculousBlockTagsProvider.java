package dev.thomasglasser.mineraculous.data.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedBlockTagsProvider;
import dev.thomasglasser.tommylib.api.data.tags.ExtendedIntrinsicHolderTagsProvider;
import dev.thomasglasser.tommylib.api.tags.ConventionalBlockTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class MineraculousBlockTagsProvider extends ExtendedBlockTagsProvider {
    public MineraculousBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(MineraculousBlockTags.CATACLYSM_IMMUNE)
                .add(Blocks.WATER)
                .add(Blocks.LAVA)
                .add(Blocks.FIRE)
                .add(Blocks.SOUL_FIRE)
                .add(MineraculousBlocks.CATACLYSM_BLOCK.get())
                .addOptionalTag(ConventionalBlockTags.UNBREAKABLE_BLOCKS);

        tag(MineraculousBlockTags.CHEESE_BLOCKS_FOODS)
                .addTag(MineraculousBlockTags.CHEESE_BLOCKS)
                .addTag(MineraculousBlockTags.CAMEMBERT_BLOCKS);

        ExtendedIntrinsicHolderTagsProvider.ExtendedIntrinsicTagAppender<Block> cheeseBlocks = tag(MineraculousBlockTags.CHEESE_BLOCKS);
        MineraculousBlocks.CHEESE_BLOCKS.values().forEach(cheeseBlocks::add);
        ExtendedIntrinsicHolderTagsProvider.ExtendedIntrinsicTagAppender<Block> camembertBlocks = tag(MineraculousBlockTags.CAMEMBERT_BLOCKS);
        MineraculousBlocks.CAMEMBERT_BLOCKS.values().forEach(camembertBlocks::add);
    }
}
