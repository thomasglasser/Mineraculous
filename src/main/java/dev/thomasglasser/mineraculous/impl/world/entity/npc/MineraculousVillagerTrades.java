package dev.thomasglasser.mineraculous.impl.world.entity.npc;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class MineraculousVillagerTrades {
    private static final int RARE_SUPPLY = 2;
    private static final int VERY_RARE_SUPPLY = 1;

    public static void onRegisterVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        if (event.getType() == MineraculousVillagerProfessions.FROMAGER.get()) {
            trades.get(1).addAll(ImmutableList.of(
                    new VillagerTrades.EmeraldForItems(Items.MILK_BUCKET, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_1_BUY, 5),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.FRESH).get(), 2, 1, VillagerTrades.COMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_1_SELL)));
            trades.get(2).addAll(ImmutableList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.FRESH).get(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.AGED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.FRESH).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL)));
            trades.get(3).addAll(ImmutableList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.AGED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.RIPENED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.FRESH).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.AGED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL)));
            trades.get(4).addAll(ImmutableList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.RIPENED).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.EXQUISITE).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.AGED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.RIPENED).get(), 4, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL)));
            trades.get(5).addAll(ImmutableList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.EXQUISITE).asItem(), 6, 1, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CHEESE.get(AgeingCheese.Age.TIME_HONORED).get(), 2, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.TIME_HONORED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.RIPENED).asItem(), 12, 1, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.EXQUISITE).get(), 6, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.EXQUISITE).asItem(), 18, 1, RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).get(), 8, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new VillagerTrades.ItemsForEmeralds(MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.TIME_HONORED).asItem(), 24, 1, VERY_RARE_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE)));
        } else if (event.getType() == MineraculousVillagerProfessions.BAKER.get()) {
            trades.get(1).addAll(ImmutableList.of(
                    // TODO: Buys almonds
                    new VillagerTrades.EmeraldForItems(Items.WHEAT, 2, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_1_BUY, 4)));
            trades.get(2).addAll(ImmutableList.of(
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.RAW_MACARON.get(), 2, 1, VillagerTrades.COMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_2_SELL)));
            trades.get(3).addAll(ImmutableList.of(
                    new VillagerTrades.DyedArmorForEmeralds(MineraculousItems.RAW_MACARON.get(), 4, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL),
                    new VillagerTrades.DyedArmorForEmeralds(MineraculousItems.RAW_MACARON.get(), 4, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_3_SELL)));
            trades.get(4).addAll(ImmutableList.of(
                    new VillagerTrades.DyedArmorForEmeralds(MineraculousItems.RAW_MACARON.get(), 4, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.DyedArmorForEmeralds(MineraculousItems.RAW_MACARON.get(), 4, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL),
                    new VillagerTrades.ItemsForEmeralds(MineraculousItems.MACARON.get(), 4, 1, VillagerTrades.COMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_4_SELL)));
            trades.get(5).addAll(ImmutableList.of(
                    new UndyeableDyedItemForEmeralds(MineraculousItems.MACARON.get(), 8, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new UndyeableDyedItemForEmeralds(MineraculousItems.MACARON.get(), 8, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE),
                    new UndyeableDyedItemForEmeralds(MineraculousItems.MACARON.get(), 8, VillagerTrades.UNCOMMON_ITEMS_SUPPLY, VillagerTrades.XP_LEVEL_5_TRADE)));
        }
    }

    public static class UndyeableDyedItemForEmeralds implements VillagerTrades.ItemListing {
        private final Item item;
        private final int value;
        private final int maxUses;
        private final int villagerXp;

        public UndyeableDyedItemForEmeralds(Item item, int value) {
            this(item, value, VillagerTrades.DEFAULT_SUPPLY, VillagerTrades.XP_LEVEL_1_SELL);
        }

        public UndyeableDyedItemForEmeralds(Item item, int value, int maxUses, int villagerXp) {
            this.item = item;
            this.value = value;
            this.maxUses = maxUses;
            this.villagerXp = villagerXp;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            ItemCost itemcost = new ItemCost(Items.EMERALD, this.value);
            ItemStack itemstack = new ItemStack(this.item);
            ImmutableList.Builder<DyeItem> list = new ImmutableList.Builder<>();
            list.add(getRandomDye(random));
            if (random.nextFloat() > 0.7F) {
                list.add(getRandomDye(random));
            }

            if (random.nextFloat() > 0.8F) {
                list.add(getRandomDye(random));
            }

            itemstack = MineraculousItemUtils.applyDyesToUndyeable(itemstack, list.build());

            return new MerchantOffer(itemcost, itemstack, this.maxUses, this.villagerXp, 0.2F);
        }

        private static DyeItem getRandomDye(RandomSource random) {
            return DyeItem.byColor(DyeColor.byId(random.nextInt(DyeColor.values().length)));
        }
    }
}
