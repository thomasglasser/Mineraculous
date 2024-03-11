package dev.thomasglasser.miraculous.data.lang;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.world.item.MiraculousCreativeModeTabs;
import dev.thomasglasser.miraculous.world.item.MiraculousItems;
import dev.thomasglasser.miraculous.world.item.armor.MiraculousArmors;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedLanguageProvider;
import net.minecraft.data.PackOutput;

public class MiraculousEnUsLanguageProvider extends ExtendedLanguageProvider
{
	public MiraculousEnUsLanguageProvider(PackOutput output)
	{
		super(output, Miraculous.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations()
	{
		add(MiraculousItems.CAT_MIRACULOUS.get(), "Cat Miraculous");

		addCreativeTab(MiraculousCreativeModeTabs.MIRACULOUS, "Miraculous");

		MiraculousArmors.MIRACULOUS_SETS.forEach(set ->
		{
			add(set.HEAD.get(), set.getDisplayName() + " Mask");
			add(set.CHEST.get(), set.getDisplayName() + " Chestplate");
			add(set.LEGS.get(), set.getDisplayName() + " Leggings");
			add(set.FEET.get(), set.getDisplayName() + " Boots");
		});
	}
}