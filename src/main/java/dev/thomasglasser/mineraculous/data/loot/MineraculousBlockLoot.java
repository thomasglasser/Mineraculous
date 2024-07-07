package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.loot.ExtendedBlockLootSubProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;

public class MineraculousBlockLoot extends ExtendedBlockLootSubProvider {
    protected MineraculousBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider, MineraculousBlocks.BLOCKS);
    }

    @Override
    protected void generate() {
        dropSelf(MineraculousBlocks.CHEESE_POT.get());

        dropOther(MineraculousBlocks.CATACLYSM_BLOCK.get(), MineraculousItems.CATACLYSM_DUST.get());

        MineraculousBlocks.CHEESE_BLOCKS.values().stream().map(DeferredHolder::get).forEach(block -> dropWithProperties(block, CheeseBlock.BITES));
        MineraculousBlocks.WAXED_CHEESE_BLOCKS.values().stream().map(DeferredHolder::get).forEach(block -> dropWithProperties(block, CheeseBlock.BITES));
        MineraculousBlocks.CAMEMBERT_BLOCKS.values().stream().map(DeferredHolder::get).forEach(block -> dropWithProperties(block, CheeseBlock.BITES));
        MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS.values().stream().map(DeferredHolder::get).forEach(block -> dropWithProperties(block, CheeseBlock.BITES));
    }
}
