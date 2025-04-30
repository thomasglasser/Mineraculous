package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
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
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class MineraculousItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mineraculous.MOD_ID);

    // Tools
    public static final DeferredItem<Item> LADYBUG_YOYO = register("ladybug_yoyo", () -> new LadybugYoyoItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", () -> new CatStaffItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> BUTTERFLY_CANE = register("butterfly_cane", () -> new ButterflyCaneItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC)));

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
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CHEESE_WEDGES = wedges("cheese", MineraculousFoods.CHEESE, () -> MineraculousBlocks.CHEESE_BLOCKS);
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> WAXED_CHEESE_WEDGES = waxedWedges("cheese", () -> MineraculousBlocks.WAXED_CHEESE_BLOCKS);
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CAMEMBERT_WEDGES = wedges("camembert", MineraculousFoods.CAMEMBERT, () -> MineraculousBlocks.CAMEMBERT_BLOCKS);
    public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> WAXED_CAMEMBERT_WEDGES = waxedWedges("camembert", () -> MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);

    private static SortedMap<CheeseBlock.Age, DeferredItem<?>> wedges(String name, FoodProperties foodProperties, Supplier<SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>>> blocks) {
        SortedMap<CheeseBlock.Age, DeferredItem<?>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, register(age.getSerializedName() + "_wedge_of_" + name, () -> new ItemNameBlockStateItem(blocks.get().get(age).get().defaultBlockState().setValue(CheeseBlock.BITES, CheeseBlock.MAX_BITES), new Item.Properties().stacksTo(16).food(foodProperties))));
        return cheese;
    }

    private static SortedMap<CheeseBlock.Age, DeferredItem<?>> waxedWedges(String name, Supplier<SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>>> blocks) {
        SortedMap<CheeseBlock.Age, DeferredItem<?>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
        for (CheeseBlock.Age age : CheeseBlock.Age.values())
            cheese.put(age, register("waxed_" + age.getSerializedName() + "_wedge_of_" + name, () -> new ItemNameBlockStateItem(blocks.get().get(age).get().defaultBlockState().setValue(CheeseBlock.BITES, CheeseBlock.MAX_BITES), new Item.Properties().stacksTo(16))));
        return cheese;
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

    public static void init() {}
}
