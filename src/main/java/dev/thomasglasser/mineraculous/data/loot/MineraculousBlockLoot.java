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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class MineraculousBlockLoot extends ExtendedBlockLootSubProvider {
    protected MineraculousBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider, MineraculousBlocks.BLOCKS);
    }

    @Override
    protected void generate() {
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);

        dropSelf(MineraculousBlocks.CHEESE_POT.get());

        dropOther(MineraculousBlocks.CATACLYSM_BLOCK.get(), MineraculousItems.CATACLYSM_DUST.get());

        cheese(MineraculousBlocks.CHEESE_BLOCKS);
        waxed(MineraculousBlocks.WAXED_CHEESE_BLOCKS);
        cheese(MineraculousBlocks.CAMEMBERT_BLOCKS);
        waxed(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);

        this.add(
                MineraculousBlocks.HIBISCUS_BUSH.get(),
                p_249159_ -> this.applyExplosionDecay(
                        p_249159_,
                        LootTable.lootTable()
                                .withPool(
                                        LootPool.lootPool()
                                                .when(
                                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(MineraculousBlocks.HIBISCUS_BUSH.get())
                                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3)))
                                                .add(LootItem.lootTableItem(MineraculousBlocks.HIBISCUS_BUSH.asItem()))
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                                                .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE))))
                                .withPool(
                                        LootPool.lootPool()
                                                .when(
                                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(MineraculousBlocks.HIBISCUS_BUSH.get())
                                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2)))
                                                .add(LootItem.lootTableItem(MineraculousBlocks.HIBISCUS_BUSH.asItem()))
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                                .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE))))));
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
