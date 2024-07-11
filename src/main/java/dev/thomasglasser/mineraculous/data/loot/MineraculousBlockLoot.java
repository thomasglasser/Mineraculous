package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.loot.ExtendedBlockLootSubProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import java.util.Set;
import java.util.SortedMap;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class MineraculousBlockLoot extends ExtendedBlockLootSubProvider {
    protected MineraculousBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider, MineraculousBlocks.BLOCKS);
    }

    @Override
    protected void generate() {
        dropSelf(MineraculousBlocks.CHEESE_POT.get());

        dropOther(MineraculousBlocks.CATACLYSM_BLOCK.get(), MineraculousItems.CATACLYSM_DUST.get());

        cheese(MineraculousBlocks.CHEESE_BLOCKS);
        waxed(MineraculousBlocks.WAXED_CHEESE_BLOCKS);
        cheese(MineraculousBlocks.CAMEMBERT_BLOCKS);
        waxed(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);
    }

    protected void cheese(SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> cheese) {
        cheese.values().stream().map(DeferredHolder::get).forEach(block -> add(block, this.createCheeseTable(block)));
    }

    protected void waxed(SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed) {
        waxed.values().stream().map(DeferredHolder::get).forEach(block -> dropWithProperties(block, CheeseBlock.BITES));
    }

    protected LootTable.Builder createCheeseTable(CheeseBlock block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block.getWedge()).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CheeseBlock.BITES, CheeseBlock.MAX_BITES))).otherwise(LootItem.lootTableItem(block).apply(CopyBlockState.copyState(block).copy(CheeseBlock.BITES))))));
    }
}
