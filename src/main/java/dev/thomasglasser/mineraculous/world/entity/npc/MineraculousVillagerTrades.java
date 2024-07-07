package dev.thomasglasser.mineraculous.world.entity.npc;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;

public class MineraculousVillagerTrades {
    public static final int RARE_SUPPLY = 2;
    public static final int VERY_RARE_SUPPLY = 1;

    public static final Map<DeferredHolder<VillagerProfession, ?>, Int2ObjectMap<List<VillagerTrades.ItemListing>>> TRADES = Map.of(
            MineraculousVillagerProfessions.FROMAGER, new Int2ObjectLinkedOpenHashMap<>(
                    Map.of(
                            1, List.of(
                                    new VillagerTrades.EmeraldForItems(Items.MILK_BUCKET, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_1_BUY, 5),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.FRESH).get(), 2, 1, VillagerTrades.COMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_1_SELL)),
                            2, List.of(
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.AGED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL)),
                            3, List.of(
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.AGED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.RIPENED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.FRESH).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.AGED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL)),
                            4, List.of(
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.RIPENED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.EXQUISITE).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.AGED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.RIPENED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL)),
                            5, List.of(
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.EXQUISITE).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE_WEDGES.get(CheeseBlock.Age.TIME_HONORED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.RIPENED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.EXQUISITE).get(), 6, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.EXQUISITE).asItem(), 18, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.TIME_HONORED).get(), 8, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT_BLOCKS.get(CheeseBlock.Age.TIME_HONORED).asItem(), 24, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE)))));
}
