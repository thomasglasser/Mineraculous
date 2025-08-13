package dev.thomasglasser.mineraculous.impl.data.loot;

import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.world.level.storage.loot.MineraculousGiftLootKeys;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public record MineraculousGiftLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        LootPool.Builder fromagerPool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        MineraculousBlocks.CHEESE.values().forEach(item -> fromagerPool.add(LootItem.lootTableItem(item)));
        MineraculousBlocks.CAMEMBERT.values().forEach(item -> fromagerPool.add(LootItem.lootTableItem(item)));
        output.accept(MineraculousGiftLootKeys.FROMAGER_GIFT, LootTable.lootTable().withPool(fromagerPool));
    }
}
