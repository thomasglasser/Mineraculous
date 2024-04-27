package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class MineraculousCreativeModeTabs
{
	public static final RegistrationProvider<CreativeModeTab> TABS = RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, Mineraculous.MOD_ID);

	public static final RegistryObject<CreativeModeTab> MINERACULOUS = TABS.register(Mineraculous.MOD_ID, () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc(Mineraculous.MOD_ID).toLanguageKey("item_group")), () -> MineraculousItems.CAT_MIRACULOUS.get().getDefaultInstance() /*TODO: Ladybug miraculous*/, true, (parameters, output) ->
			output.acceptAll(MineraculousItems.getItemsInModTab().stream().map(ro -> ro.get().getDefaultInstance()).toList())
	));

	public static final RegistryObject<CreativeModeTab> SUITS = TABS.register("suits", () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc("suits").toLanguageKey("item_group")), () -> MineraculousArmors.CAT_MIRACULOUS.HEAD.get().getDefaultInstance(), true, (parameters, output) ->
			output.acceptAll(MineraculousArmors.MIRACULOUS_SETS.stream().flatMap(set -> set.getAll().stream()).map(ro -> ro.get().getDefaultInstance()).toList())
	));

	public static void init() {}
}
