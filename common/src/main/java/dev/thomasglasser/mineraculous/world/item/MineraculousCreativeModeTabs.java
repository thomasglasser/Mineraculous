package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.ArrayList;
import java.util.List;

public class MineraculousCreativeModeTabs
{
	public static final RegistrationProvider<CreativeModeTab> TABS = RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, Mineraculous.MOD_ID);

	public static final RegistryObject<CreativeModeTab> MINERACULOUS = TABS.register(Mineraculous.MOD_ID, () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc(Mineraculous.MOD_ID).toLanguageKey("item_group")), () -> MineraculousItems.CAT_MIRACULOUS.get().getDefaultInstance(), true, (parameters, output) ->
	{
		List<ResourceLocation> itemsToAdd = new ArrayList<>();

		ItemUtils.getItemTabs().values().forEach(rls ->
				rls.forEach(rl ->
				{
					if (!itemsToAdd.contains(rl))
						itemsToAdd.add(rl);
				}));

		output.acceptAll(itemsToAdd.stream().map(rl -> BuiltInRegistries.ITEM.get(rl).getDefaultInstance()).toList());
	}, CreativeModeTabs.SPAWN_EGGS));

	public static final RegistryObject<CreativeModeTab> SUITS = TABS.register("suits", () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc("suits").toLanguageKey("item_group")), () -> MineraculousArmors.CAT_MIRACULOUS.HEAD.get().getDefaultInstance(), true, (parameters, output) ->
	{
		List<ResourceLocation> itemsToAdd = new ArrayList<>();

		MineraculousArmors.MIRACULOUS_SETS.forEach(set -> set.getAll().forEach(ro -> itemsToAdd.add(ro.getId())));

		output.acceptAll(itemsToAdd.stream().map(rl -> BuiltInRegistries.ITEM.get(rl).getDefaultInstance()).toList());
	}, CreativeModeTabs.COMBAT));

	public static void init() {}
}
