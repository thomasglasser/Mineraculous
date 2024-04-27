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
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
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
		add(MineraculousItems.CAT_STAFF.get(), "Cat Staff");
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

		add(MineraculousBlocks.CATACLYSM_BLOCK.get(), "Block of Cataclysm");

		addConfigs();

		add(MineraculousEntityTypes.PLAGG.get(), "Plagg");

		add(MineraculousKeyMappings.MIRACULOUS_CATEGORY, "Miraculous");
		add(MineraculousKeyMappings.TRANSFORM, "Transform");
		add(MineraculousKeyMappings.ACTIVATE_MAIN_POWER, "Activate Main Power");
		add(MineraculousKeyMappings.OPEN_ABILITY_WHEEL, "Open Power Wheel");
		add(MineraculousKeyMappings.ACTIVATE_TOOL, "Activate Tool");
		add(MineraculousKeyMappings.ACTIVATE_TRAVELLING, "Activate Travelling");

		add(MiraculousData.NAME_NOT_SET, "You haven't set your %s hero name yet! Use /miraculous %s name <name> to set.");

		add(MiraculousCommand.NOT_SET, "not set");
		add(MiraculousCommand.NAME_QUERY_SUCCESS_SELF, "Your %s hero name is %s.");
		add(MiraculousCommand.NAME_QUERY_SUCCESS_OTHER, "%s's %s hero name is %s.");
		add(MiraculousCommand.NAME_SET_SUCCESS_SELF, "Your %s hero name has been set to %s.");
		add(MiraculousCommand.NAME_SET_SUCCESS_OTHER, "Set %s's %s hero name to %s.");
		add(MiraculousCommand.NAME_CLEAR_SUCCESS_SELF, "Your %s hero name has been cleared.");
		add(MiraculousCommand.NAME_CLEAR_SUCCESS_OTHER, "Cleared %s's %s hero name.");
		add(MiraculousCommand.CHARGED_TRUE, "charged");
		add(MiraculousCommand.CHARGED_FALSE, "not charged");
		add(MiraculousCommand.CHARGED_QUERY_SUCCESS_SELF, "Your %s kwami is %s.");
		add(MiraculousCommand.CHARGED_QUERY_SUCCESS_OTHER, "%s's %s kwami is %s.");
		add(MiraculousCommand.CHARGED_SET_SUCCESS_SELF, "Set charged state of %s kwami to %s");
		add(MiraculousCommand.CHARGED_SET_SUCCESS_OTHER, "Set charged state of %s's %s kwami to %s");
		add(MiraculousCommand.POWER_LEVEL_QUERY_SUCCESS_SELF, "Your %s power level is %s.");
		add(MiraculousCommand.POWER_LEVEL_QUERY_SUCCESS_OTHER, "%s's %s power level is %s.");
		add(MiraculousCommand.POWER_LEVEL_SET_SUCCESS_SELF, "Your %s power level has been set to %s.");
		add(MiraculousCommand.POWER_LEVEL_SET_SUCCESS_OTHER, "Set %s's %s power level to %s.");
		add(MiraculousCommand.NOT_LIVING_ENTITY, "Target must be a living entity.");
		add(MiraculousCommand.TRANSFORMED, "This command cannot be executed while %s is transformed.");
		add(MiraculousCommand.KWAMI_NOT_FOUND, "%s's %s kwami not found in the world");

		add(MiraculousType.CAT, "Cat");
	}

	private void addConfigs()
	{
		addConfigTitle(Mineraculous.MOD_NAME);

		addConfigCategory("miraculous", "Miraculous");
		addConfig("miraculous_comment", MineraculousClientConfig.miraculous_comment);
		addConfig("enable_per_player_customization_comment", MineraculousClientConfig.enable_per_player_customization_comment);
		addConfig("enablePerPlayerCustomization", "Enable Per Player Customization");
	}

	protected void add(MiraculousType type, String name)
	{
		add(type.getTranslationKey(), name);
	}
}
