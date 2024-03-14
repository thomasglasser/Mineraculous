package dev.thomasglasser.miraculous.world.item;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.world.entity.MiraculousEntityTypes;
import dev.thomasglasser.miraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.miraculous.world.item.armor.MiraculousArmors;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;
import java.util.function.Supplier;

public class MiraculousItems
{
	public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Miraculous.MOD_ID);

	public static final RegistryObject<MiraculousItem> CAT_MIRACULOUS = registerMiraculous("cat", MiraculousArmors.CAT_MIRACULOUS, null /*TODO: Stick */, null /* TODO: Transform sound */, MiraculousEntityTypes.PLAGG::get);

	// SPAWN EGGS
	public static final RegistryObject<SpawnEggItem> PLAGG_SPAWN_EGG = registerSpawnEgg("plagg_spawn_egg", MiraculousEntityTypes.PLAGG::get, 0x1c1b20, 0xb6fa02);

	private static RegistryObject<MiraculousItem> registerMiraculous(String name, ArmorSet armorSet, Supplier<Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType)
	{
		return register(name + "_miraculous", () -> new MiraculousItem(new Item.Properties(), armorSet, tool, transformSound, kwamiType), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return ItemUtils.register(ITEMS, name, item, tabs);
	}

	private static RegistryObject<SpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<? extends Mob>> entityType, int primaryColor, int secondaryColor)
	{
		return ItemUtils.registerSpawnEgg(ITEMS, name, entityType, primaryColor, secondaryColor);
	}

	public static void init() {}
}
