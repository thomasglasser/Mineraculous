package dev.thomasglasser.mineraculous.world.entity.npc;

import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class MineraculousVillagerTrades {
    public static final int RARE_SUPPLY = 2;
    public static final int VERY_RARE_SUPPLY = 1;

    public static void onRegisterVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        if (event.getType() == MineraculousVillagerProfessions.FROMAGER.get()) {
            // Fromager
            trades.put(1, ReferenceArrayList.of(
                    new VillagerTrades.EmeraldForItems(Items.MILK_BUCKET, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_1_BUY, 5),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.FRESH).get(), 2, 1, VillagerTrades.COMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_1_SELL)
            ));
            trades.put(2, ReferenceArrayList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.FRESH).get(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.AGED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.FRESH).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL)
            ));
            trades.put(3, ReferenceArrayList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.AGED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.RIPENED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.FRESH).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.AGED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL)
            ));
            trades.put(4, ReferenceArrayList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.RIPENED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.EXQUISITE).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.AGED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.RIPENED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL)
            ));
            trades.put(5, ReferenceArrayList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.EXQUISITE).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.TIME_HONORED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.TIME_HONORED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.RIPENED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.EXQUISITE).get(), 6, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.EXQUISITE).asItem(), 18, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).get(), 8, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).asItem(), 24, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE)
            ));
        }
    }
}
