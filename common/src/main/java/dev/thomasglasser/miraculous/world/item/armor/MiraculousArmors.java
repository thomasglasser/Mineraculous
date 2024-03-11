package dev.thomasglasser.miraculous.world.item.armor;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class MiraculousArmors
{
	public static final RegistrationProvider<Item> ARMORS = RegistrationProvider.get(Registries.ITEM, Miraculous.MOD_ID);
	public static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().stacksTo(1);
	public static final List<ArmorSet> MIRACULOUS_SETS = new ArrayList<>();

	public static final ArmorSet CAT_MIRACULOUS = createMiraculousSet("cat", "Cat");

	private static ArmorSet createMiraculousSet(String name, String displayName)
	{
		RegistryObject<ArmorItem> mask = ItemUtils.register(ARMORS, name + "_miraculous_mask", () -> new MiraculousArmorItem(name, ArmorItem.Type.HELMET, DEFAULT_PROPERTIES), List.of(CreativeModeTabs.COMBAT));
		RegistryObject<ArmorItem> chestplate = ItemUtils.register(ARMORS, name + "_miraculous_chestplate", () -> new MiraculousArmorItem(name, ArmorItem.Type.CHESTPLATE, DEFAULT_PROPERTIES), List.of(CreativeModeTabs.COMBAT));
		RegistryObject<ArmorItem> leggings = ItemUtils.register(ARMORS, name + "_miraculous_leggings", () -> new MiraculousArmorItem(name, ArmorItem.Type.LEGGINGS, DEFAULT_PROPERTIES), List.of(CreativeModeTabs.COMBAT));
		RegistryObject<ArmorItem> boots = ItemUtils.register(ARMORS, name + "_miraculous_boots", () -> new MiraculousArmorItem(name, ArmorItem.Type.BOOTS, DEFAULT_PROPERTIES), List.of(CreativeModeTabs.COMBAT));

		ArmorSet set = new ArmorSet(name + "_miraculous", displayName + " Miraculous", mask, chestplate, leggings, boots);
		MIRACULOUS_SETS.add(set);
		return set;
	}

	public static void init() {}
}