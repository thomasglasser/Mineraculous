package dev.thomasglasser.mineraculous.impl.data.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
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
        super(output, lookupProvider, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addCheeses();
        addAbilities();
        addTrees();
    }

    private void addCheeses() {
        tag(MineraculousBlockTags.CHEESE_BLOCKS_FOODS)
                .addTag(MineraculousBlockTags.CHEESE_BLOCKS)
                .addTag(MineraculousBlockTags.CAMEMBERT_BLOCKS);

        ExtendedIntrinsicHolderTagsProvider.ExtendedIntrinsicTagAppender<Block> cheeseBlocks = tag(MineraculousBlockTags.CHEESE_BLOCKS);
        MineraculousBlocks.CHEESE.values().forEach(cheeseBlocks::add);
        ExtendedIntrinsicHolderTagsProvider.ExtendedIntrinsicTagAppender<Block> camembertBlocks = tag(MineraculousBlockTags.CAMEMBERT_BLOCKS);
        MineraculousBlocks.CAMEMBERT.values().forEach(camembertBlocks::add);
    }

    private void addAbilities() {
        // Cataclysm
        tag(MineraculousBlockTags.CATACLYSM_IMMUNE)
                .add(Blocks.WATER)
                .add(Blocks.LAVA)
                .add(Blocks.FIRE)
                .add(Blocks.SOUL_FIRE)
                .add(MineraculousBlocks.CATACLYSM_BLOCK.get())
                .addOptionalTag(ConventionalBlockTags.UNBREAKABLE_BLOCKS);
    }

    private void addTrees() {
        // Almond WoodSet & LeavesSet
        woodSet(MineraculousBlocks.ALMOND_WOOD_SET);
        leavesSet(MineraculousBlocks.ALMOND_LEAVES_SET);
    }
}
