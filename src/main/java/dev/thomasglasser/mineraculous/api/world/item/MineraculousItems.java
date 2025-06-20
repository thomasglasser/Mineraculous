package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.mineraculous.impl.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.impl.world.level.block.CheeseBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mineraculous.MOD_ID);

    // Tools
    public static final Supplier<Item.Properties> TOOL_PROPERTIES = () -> new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC);
    public static final Supplier<Item.Properties> TOOL_PROPERTIES_WITH_ACTIVE = () -> TOOL_PROPERTIES.get().component(MineraculousDataComponents.ACTIVE, false);
    public static final DeferredItem<Item> LADYBUG_YOYO = register("ladybug_yoyo", () -> new LadybugYoyoItem(TOOL_PROPERTIES_WITH_ACTIVE.get()));
    public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", () -> new CatStaffItem(TOOL_PROPERTIES_WITH_ACTIVE.get().component(MineraculousDataComponents.ACTIVE_SETTINGS, CatStaffItem.ACTIVE_SETTINGS)));
    public static final DeferredItem<Item> BUTTERFLY_CANE = register("butterfly_cane", () -> new ButterflyCaneItem(TOOL_PROPERTIES.get()));

    // Miraculous
    public static final DeferredItem<MiraculousItem> MIRACULOUS = register("miraculous", () -> new MiraculousItem(new Item.Properties()));

    public static final DeferredItem<Item> CATACLYSM_DUST = register("cataclysm_dust", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // Smithing Templates
    public static final DeferredItem<SmithingTemplateItem> LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.LADYBUG);
    public static final DeferredItem<SmithingTemplateItem> CAT_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.CAT);
    public static final DeferredItem<SmithingTemplateItem> BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.BUTTERFLY);

    // Spawn Eggs
    public static final DeferredItem<SpawnEggItem> KAMIKO_SPAWN_EGG = registerSpawnEgg(MineraculousEntityTypes.KAMIKO, 0xc8e5ea, 0x140325);

    // Cheese
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> CHEESE = wedges("cheese", MineraculousFoods.CHEESE, MineraculousBlocks.CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> WAXED_CHEESE = waxedWedges("cheese", MineraculousBlocks.WAXED_CHEESE);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> CAMEMBERT = wedges("camembert", MineraculousFoods.CAMEMBERT, MineraculousBlocks.CAMEMBERT);
    public static final SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> WAXED_CAMEMBERT = waxedWedges("camembert", MineraculousBlocks.WAXED_CAMEMBERT);

    private static SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> wedges(String name, FoodProperties foodProperties, SortedMap<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> blocks) {
        Item.Properties properties = new Item.Properties().stacksTo(16).food(foodProperties).component(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CheeseBlock.BITES, CheeseBlock.MAX_BITES));
        SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, register(age.getSerializedName() + "_" + name + "_wedge", () -> new ItemNameBlockItem(blocks.get(age).get(), properties) {
                @Override
                public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {}
            }));
        }
        return cheeses;
    }

    private static SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> waxedWedges(String name, SortedMap<AgeingCheese.Age, DeferredBlock<CheeseBlock>> blocks) {
        Item.Properties properties = new Item.Properties().stacksTo(16).component(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CheeseBlock.BITES, CheeseBlock.MAX_BITES));
        SortedMap<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> cheeses = new Reference2ObjectLinkedOpenHashMap<>(AgeingCheese.Age.values().length);
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            cheeses.put(age, register("waxed_" + age.getSerializedName() + "_" + name + "_wedge", () -> new ItemNameBlockItem(blocks.get(age).get(), properties) {
                @Override
                public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {}
            }));
        }
        return cheeses;
    }

    public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item) {
        return ItemUtils.register(ITEMS, name, item);
    }

    private static <T extends Mob> DeferredItem<SpawnEggItem> registerSpawnEgg(DeferredHolder<EntityType<?>, EntityType<T>> entityType, int primaryColor, int secondaryColor) {
        return register(entityType.getKey().location().getPath() + "_spawn_egg", () -> new DeferredSpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties()));
    }

    private static DeferredItem<SmithingTemplateItem> registerSmithingTemplate(ResourceKey<TrimPattern> pattern) {
        return ItemUtils.registerSmithingTemplate(ITEMS, pattern);
    }

    @ApiStatus.Internal
    public static void init() {}
}
