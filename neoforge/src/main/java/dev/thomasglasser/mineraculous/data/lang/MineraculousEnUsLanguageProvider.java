package dev.thomasglasser.mineraculous.data.lang;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
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
		add(MineraculousItems.CATACLYSM_DUST.get(), "Cataclysm Dust");

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

		add(MiraculousData.NAME_NOT_SET, "You haven't set your %s hero name yet! Use /miraculous %s name <name> to set.");

		add(MiraculousCommand.NAME_SUCCESS_SELF, "Your %s hero name has been set to %s.");
		add(MiraculousCommand.NAME_SUCCESS_OTHER, "Set %3s's %1s hero name to %2s.");
		add(MiraculousCommand.NAME_CLEAR_SUCCESS_SELF, "Your %s hero name has been cleared.");
		add(MiraculousCommand.NAME_CLEAR_SUCCESS_OTHER, "Cleared %2s's %1s hero name.");
		add(MiraculousCommand.NOT_LIVING_ENTITY, "Target must be a living entity.");

		add(MiraculousType.CAT, "Cat");
	}

	private void addConfigs()
	{
		addConfigTitle(Mineraculous.MOD_NAME);

		addConfigCategory("miraculous", "Miraculous");
		addConfig("miraculous_comment", MineraculousClientConfig.miraculous_comment);
		addConfig("enable_custom_hidden_variants_comment", MineraculousClientConfig.enable_custom_hidden_variants_comment);
		addConfig("enableCustomHiddenVariants", "Enable Custom Hidden Variants");
	}

	protected void add(MiraculousType type, String name)
	{
		add(type.getTranslationKey(), name);
	}
}
