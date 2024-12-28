package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemNameBlockStateItem;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.armortrim.TrimPattern;

public class MineraculousItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mineraculous.MOD_ID);

    private static final Supplier<Item> BASIC_ITEM = () -> new Item(new Item.Properties());

    // Tools
    public static final DeferredItem<Item> LADYBUG_YOYO = register("ladybug_yoyo", () -> new LadybugYoyoItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", () -> new CatStaffItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> BUTTERFLY_CANE = register("butterfly_cane", BASIC_ITEM);

    // Miraculous
    public static final DeferredItem<MiraculousItem> MIRACULOUS = register("miraculous", () -> new MiraculousItem(new Item.Properties()));

    public static final DeferredItem<Item> CATACLYSM_DUST = register("cataclysm_dust", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // Smithing Templates
    public static final DeferredItem<SmithingTemplateItem> LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.LADYBUG);
    public static final DeferredItem<SmithingTemplateItem> CAT_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.CAT);
    public static final DeferredItem<SmithingTemplateItem> BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE = registerSmithingTemplate(MineraculousTrimPatterns.BUTTERFLY);

    // Spawn Eggs
    public static final DeferredItem<SpawnEggItem> KAMIKO_SPAWN_EGG = registerSpawnEgg("kamiko_spawn_egg", MineraculousEntityTypes.KAMIKO::get, 0xc8e5ea, 0x140325);

    // Cheese
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CHEESE_WEDGES = wedges("cheese", MineraculousFoods.CHEESE, () -> MineraculousBlocks.CHEESE_BLOCKS);
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CAMEMBERT_WEDGES = wedges("camembert", MineraculousFoods.CAMEMBERT, () -> MineraculousBlocks.CAMEMBERT_BLOCKS);

    private static SortedMap<CheeseBlock.Age, DeferredItem<?>> wedges(String name, FoodProperties foodProperties, Supplier<SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>>> blocks) {
        SortedMap<CheeseBlock.Age, DeferredItem<?>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, register(age.getSerializedName() + "_wedge_of_" + name, () -> new ItemNameBlockStateItem(blocks.get().get(age).get().defaultBlockState().setValue(CheeseBlock.BITES, CheeseBlock.MAX_BITES), new Item.Properties().food(foodProperties))));
        return cheese;
    }

    public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item) {
        return ItemUtils.register(ITEMS, name, item);
    }

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor) {
        return ItemUtils.registerSpawnEgg(ITEMS, name, entityType, primaryColor, secondaryColor);
    }

    private static DeferredItem<SmithingTemplateItem> registerSmithingTemplate(ResourceKey<TrimPattern> pattern) {
        return ItemUtils.registerSmithingTemplate(ITEMS, pattern);
    }

    public static void init() {}
}
