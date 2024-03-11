package dev.thomasglasser.miraculous.world.item;

import dev.thomasglasser.miraculous.Miraculous;
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

public class MiraculousCreativeModeTabs
{
	public static final RegistrationProvider<CreativeModeTab> TABS = RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, Miraculous.MOD_ID);

	public static final RegistryObject<CreativeModeTab> MIRACULOUS = TABS.register(Miraculous.MOD_ID, () -> TommyLibServices.ITEM.newTab(Component.translatable(Miraculous.modLoc(Miraculous.MOD_ID).toLanguageKey("item_group")), () -> MiraculousItems.CAT_MIRACULOUS.get().getDefaultInstance(), true, (parameters, output) ->
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

	public static void init() {}
}
