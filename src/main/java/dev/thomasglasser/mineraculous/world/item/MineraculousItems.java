package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.food.MineraculousFoods;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Supplier;

public class MineraculousItems
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mineraculous.MOD_ID);

	private static final List<DeferredItem<?>> IN_MOD_TAB = new ArrayList<>();

	// Tools
	public static final DeferredItem<CatStaffItem> CAT_STAFF = register("cat_staff", () -> new CatStaffItem(new Item.Properties().fireResistant().stacksTo(1)), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));

	// Miraculous
	public static final DeferredItem<MiraculousItem> CAT_MIRACULOUS = registerMiraculous("cat", () -> new CatMiraculousItem(new Item.Properties()));

	public static final DeferredItem<Item> CATACLYSM_DUST = register("cataclysm_dust", () -> new Item(new Item.Properties()), List.of());

	// Spawn Eggs
	public static final DeferredItem<SpawnEggItem> TIKKI_SPAWN_EGG = registerSpawnEgg("tikki_spawn_egg",  MineraculousEntityTypes.TIKKI::get,  0xeb0944, 0x000000);
	public static final DeferredItem<SpawnEggItem> PLAGG_SPAWN_EGG = registerSpawnEgg("plagg_spawn_egg",  MineraculousEntityTypes.PLAGG::get,  0x1c1b20, 0xb6fa02);
	public static final DeferredItem<SpawnEggItem> KAMIKO_SPAWN_EGG = registerSpawnEgg("kamiko_spawn_egg", MineraculousEntityTypes.KAMIKO::get, 0x130122, 0xffffff);

	// Cheese
	public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CHEESE_WEDGES = wedges("cheese", MineraculousFoods.CHEESE);
	public static final SortedMap<CheeseBlock.Age, DeferredItem<?>> CAMEMBERT_WEDGES = wedges("camembert", MineraculousFoods.CAMEMBERT);

	private static SortedMap<CheeseBlock.Age, DeferredItem<?>> wedges(String name, FoodProperties foodProperties) {
		SortedMap<CheeseBlock.Age, DeferredItem<?>> cheese = new Object2ObjectLinkedOpenHashMap<>(CheeseBlock.Age.values().length);
		for (CheeseBlock.Age age: CheeseBlock.Age.values())
			cheese.put(age, register(age.getSerializedName() + "_wedge_of_" + name, () -> new Item(new Item.Properties().food(foodProperties)), List.of(CreativeModeTabs.FOOD_AND_DRINKS)));
		return cheese;
	}

	private static DeferredItem<MiraculousItem> registerMiraculous(String name, MiraculousType type, ArmorSet armorSet, Supplier<? extends Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType, String acceptableSlot, int color)
	{
		return register(name + "_miraculous", () -> new MiraculousItem(new Item.Properties(), type, armorSet, tool, transformSound, kwamiType, acceptableSlot, TextColor.fromRgb(color)), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));
	}

	private static <T extends MiraculousItem> DeferredItem<T> registerMiraculous(String name, Supplier<T> item)
	{
		return register(name + "_miraculous", item, List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));
	}

	public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs, boolean inModTab)
	{
		DeferredItem<T> obj = ItemUtils.register(ITEMS, name, item, tabs);
		if (inModTab)
			IN_MOD_TAB.add(obj);
		return obj;
	}

	public static <T extends Item> DeferredItem<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return register(name, item, tabs, true);
	}

	private static DeferredItem<SpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor)
	{
		return ItemUtils.registerSpawnEgg(ITEMS, name, entityType, primaryColor, secondaryColor);
	}

	public static List<DeferredItem<? extends Item>> getItemsInModTab()
	{
		return IN_MOD_TAB;
	}

	public static void init() {}
}
