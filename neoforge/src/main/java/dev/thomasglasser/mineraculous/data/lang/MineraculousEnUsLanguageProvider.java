package dev.thomasglasser.mineraculous.data.lang;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedLanguageProvider;
import net.minecraft.data.PackOutput;

public class MineraculousEnUsLanguageProvider extends ExtendedLanguageProvider
{
	public MineraculousEnUsLanguageProvider(PackOutput output)
	{
		super(output, Mineraculous.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations()
	{
		add(MineraculousItems.CAT_MIRACULOUS.get(), "Cat Miraculous");
		add(MineraculousItems.PLAGG_SPAWN_EGG.get(), "Plagg Spawn Egg");

		addCreativeTab(MineraculousCreativeModeTabs.MINERACULOUS, "Mineraculous");
		addCreativeTab(MineraculousCreativeModeTabs.SUITS, "Suits");

		MineraculousArmors.MIRACULOUS_SETS.forEach(set ->
		{
			add(set.HEAD.get(), set.getDisplayName() + " Mask");
			add(set.CHEST.get(), set.getDisplayName() + " Chestplate");
			add(set.LEGS.get(), set.getDisplayName() + " Leggings");
			add(set.FEET.get(), set.getDisplayName() + " Boots");
		});

		addConfigs();

		add(MineraculousEntityTypes.PLAGG.get(), "Plagg");

		add(MineraculousKeyMappings.MIRACULOUS_CATEGORY, "Miraculous");
		add(MineraculousKeyMappings.TRANSFORM, "Transform");
		add(MineraculousKeyMappings.ACTIVATE_POWER, "Activate Power");
	}

	private void addConfigs()
	{
		addConfigTitle(Mineraculous.MOD_NAME);

		addConfigCategory("miraculous", "Miraculous");
		addConfig("miraculous_comment", MineraculousClientConfig.miraculous_comment);
		addConfig("enable_custom_hidden_variants_comment", MineraculousClientConfig.enable_custom_hidden_variants_comment);
		addConfig("enableCustomHiddenVariants", "Enable Custom Hidden Variants");
	}
}
