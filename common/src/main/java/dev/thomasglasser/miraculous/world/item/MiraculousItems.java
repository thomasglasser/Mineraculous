package dev.thomasglasser.miraculous.world.item;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public class MiraculousItems
{
	public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Miraculous.MOD_ID);

	public static final RegistryObject<MiraculousItem> CAT_MIRACULOUS = registerMiraculous("cat");

	private static RegistryObject<MiraculousItem> registerMiraculous(String name)
	{
		return register(name + "_miraculous", () -> new MiraculousItem(new Item.Properties().stacksTo(1).fireResistant()), List.of(CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT));
	}

	private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item, List<ResourceKey<CreativeModeTab>> tabs)
	{
		return ItemUtils.register(ITEMS, name, item, tabs);
	}

	public static void init() {}
}
