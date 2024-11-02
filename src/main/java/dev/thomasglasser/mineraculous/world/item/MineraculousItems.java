package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.BlockStateItem;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;

public class MineraculousItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mineraculous.MOD_ID);

    private static final List<DeferredItem<?>> IN_MOD_TAB = new ArrayList<>();

    // Tools
    public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", properties -> new CatStaffItem(properties.fireResistant().stacksTo(1).rarity(Rarity.EPIC)), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));

    // Miraculous
    public static final DeferredItem<MiraculousItem> MIRACULOUS = register("miraculous", MiraculousItem::new, List.of(), false);

    public static final DeferredItem<Item> CATACLYSM_DUST = register("cataclysm_dust", properties -> new Item(properties.rarity(Rarity.EPIC)), List.of());

    // Spawn Eggs
    public static final DeferredItem<SpawnEggItem> KAMIKO_SPAWN_EGG = registerSpawnEgg("kamiko", MineraculousEntityTypes.KAMIKO::get, 0x130122, 0xffffff);

    // Cheese
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CHEESE_WEDGES = wedges("cheese", MineraculousFoods.CHEESE, () -> MineraculousBlocks.CHEESE_BLOCKS);
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CAMEMBERT_WEDGES = wedges("camembert", MineraculousFoods.CAMEMBERT, () -> MineraculousBlocks.CAMEMBERT_BLOCKS);

    private static SortedMap<CheeseBlock.Age, DeferredItem<?>> wedges(String name, FoodProperties foodProperties, Supplier<SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>>> blocks) {
        SortedMap<CheeseBlock.Age, DeferredItem<?>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, register(age.getSerializedName() + "_wedge_of_" + name, properties -> new BlockStateItem(blocks.get().get(age).get().defaultBlockState().setValue(CheeseBlock.BITES, CheeseBlock.MAX_BITES), properties.component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(1).build()).food(foodProperties)), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
        return cheese;
    }

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> item, List<ResourceKey<CreativeModeTab>> tabs, boolean inModTab) {
        DeferredItem<T> obj = ItemUtils.register(ITEMS, name, item, tabs);
        if (inModTab)
            IN_MOD_TAB.add(obj);
        return obj;
    }

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> item, List<ResourceKey<CreativeModeTab>> tabs) {
        return register(name, item, tabs, true);
    }

    public static <T extends BlockItem> DeferredItem<T> registerBlock(DeferredBlock<?> block, Function<Item.Properties, T> itemFunction, List<ResourceKey<CreativeModeTab>> tabs) {
        return ItemUtils.registerBlock(ITEMS, block, itemFunction, tabs);
    }

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor) {
        return ItemUtils.registerSpawnEgg(ITEMS, name, entityType, primaryColor, secondaryColor);
    }

    public static List<DeferredItem<? extends Item>> getItemsInModTab() {
        return IN_MOD_TAB;
    }

    public static void init() {}
}
