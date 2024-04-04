package dev.thomasglasser.mineraculous.world.item;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MineraculousItems
{
	public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Mineraculous.MOD_ID);

	private static final List<RegistryObject<? extends Item>> IN_MOD_TAB = new ArrayList<>();

	// Miraculous
	public static final RegistryObject<MiraculousItem> CAT_MIRACULOUS = registerMiraculous("cat", MiraculousType.CAT, MineraculousArmors.CAT_MIRACULOUS, null /*TODO: Stick */, null /* TODO: Transform sound */, MineraculousEntityTypes.PLAGG::get, Pair.of("hand", "ring"), 0xc6f800);

	public static final RegistryObject<Item> CATACLYSM_DUST = register("cataclysm_dust", () -> new Item(new Item.Properties()), List.of(MineraculousCreativeModeTabs.MINERACULOUS.getResourceKey()));

	// SPAWN EGGS
	public static final RegistryObject<SpawnEggItem> PLAGG_SPAWN_EGG = registerSpawnEgg("plagg_spawn_egg", MineraculousEntityTypes.PLAGG::get, 0x1c1b20, 0xb6fa02);

	private static RegistryObject<MiraculousItem> registerMiraculous(String name, MiraculousType type, ArmorSet armorSet, Supplier<Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType, Pair<String, String> acceptableSlot, int color)
	{
		return register(name + "_miraculous", () -> new MiraculousItem(new Item.Properties(), type, armorSet, tool, transformSound, kwamiType, acceptableSlot, TextColor.fromRgb(color)), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs, boolean inModTab)
	{
		RegistryObject<T> obj = ItemUtils.register(ITEMS, name, item, tabs);
		if (inModTab)
			IN_MOD_TAB.add(obj);
		return obj;
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return register(name, item, tabs, true);
	}

	private static RegistryObject<SpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor)
	{
		return ItemUtils.registerSpawnEgg(ITEMS, name, entityType, primaryColor, secondaryColor);
	}

	public static List<RegistryObject<? extends Item>> getItemsInModTab()
	{
		return IN_MOD_TAB;
	}

	public static void init() {}
}
