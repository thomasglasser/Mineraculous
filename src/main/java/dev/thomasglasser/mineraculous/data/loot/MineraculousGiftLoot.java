package dev.thomasglasser.mineraculous.data.loot;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.storage.loot.MineraculousGiftLootKeys;
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
        MineraculousItems.CHEESE_WEDGES.values().forEach(item -> fromagerPool.add(LootItem.lootTableItem(item)));
        MineraculousItems.CAMEMBERT_WEDGES.values().forEach(item -> fromagerPool.add(LootItem.lootTableItem(item)));
        output.accept(MineraculousGiftLootKeys.FROMAGER_GIFT, LootTable.lootTable().withPool(fromagerPool));
    }
}
