package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;

public class MineraculousArmors
{
	public static final DeferredRegister.Items ARMORS = DeferredRegister.createItems(Mineraculous.MOD_ID);
	public static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().stacksTo(1).rarity(Rarity.EPIC);
	public static final List<ArmorSet> MIRACULOUS_SETS = new ArrayList<>();

	public static final ArmorSet CAT_MIRACULOUS = createMiraculousSet("cat", "Cat");

	private static ArmorSet createMiraculousSet(String name, String displayName)
	{
		DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, name + "_miraculous_mask", () -> new MiraculousArmorItem(name, ArmorItem.Type.HELMET, DEFAULT_PROPERTIES), List.of());
		DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, name + "_miraculous_chestplate", () -> new MiraculousArmorItem(name, ArmorItem.Type.CHESTPLATE, DEFAULT_PROPERTIES), List.of());
		DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, name + "_miraculous_leggings", () -> new MiraculousArmorItem(name, ArmorItem.Type.LEGGINGS, DEFAULT_PROPERTIES), List.of());
		DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, name + "_miraculous_boots", () -> new MiraculousArmorItem(name, ArmorItem.Type.BOOTS, DEFAULT_PROPERTIES), List.of());

		ArmorSet set = new ArmorSet(name + "_miraculous", displayName + " Miraculous", mask, chestplate, leggings, boots);
		MIRACULOUS_SETS.add(set);
		return set;
	}

	public static void init() {}
}
