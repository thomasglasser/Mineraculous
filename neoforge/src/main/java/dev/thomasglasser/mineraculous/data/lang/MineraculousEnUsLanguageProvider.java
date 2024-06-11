package dev.thomasglasser.mineraculous.data.lang;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedLanguageProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import net.minecraft.data.PackOutput;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Map;

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
		add(MineraculousItems.TIKKI_SPAWN_EGG.get(), "Tikki Spawn Egg");
		add(MineraculousItems.PLAGG_SPAWN_EGG.get(), "Plagg Spawn Egg");
		add(MineraculousItems.KAMIKO_SPAWN_EGG.get(), "Kamiko Spawn Egg");
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

		cheese("Cheese", MineraculousItems.CHEESE_WEDGES, MineraculousBlocks.CHEESE_BLOCKS, MineraculousBlocks.WAXED_CHEESE_BLOCKS);
		cheese("Camembert", MineraculousItems.CAMEMBERT_WEDGES, MineraculousBlocks.CAMEMBERT_BLOCKS, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);

		addConfigs();

		add(MineraculousEntityTypes.TIKKI.get(), "Tikki");
		add(MineraculousEntityTypes.PLAGG.get(), "Plagg");
		add(MineraculousEntityTypes.KAMIKO.get(), "Kamiko");

		add(MineraculousKeyMappings.MIRACULOUS_CATEGORY, "Miraculous");
		add(MineraculousKeyMappings.TRANSFORM, "Transform");
		add(MineraculousKeyMappings.ACTIVATE_MAIN_POWER, "Activate Main Power");
		add(MineraculousKeyMappings.OPEN_ABILITY_WHEEL, "Open Ability Wheel");
		add(MineraculousKeyMappings.ACTIVATE_TOOL, "Activate Tool");
		add(MineraculousKeyMappings.TAKE_ITEM, "Take Item");

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

		add(MineraculousItemTags.TIKKI_FOODS, "Tikki Foods");
		add(MineraculousItemTags.TIKKI_TREATS, "Tikki Treats");
		add(MineraculousItemTags.PLAGG_FOODS, "Plagg Foods");
		add(MineraculousItemTags.PLAGG_TREATS, "Plagg Treats");
		add(MineraculousItemTags.CATACLYSM_IMMUNE, "Cataclysm Immune");
		add(MineraculousItemTags.CHEESES_FOODS, "Cheeses");
		add(MineraculousItemTags.CHEESE, "Cheese");
		add(MineraculousItemTags.CAMEMBERT, "Camembert");

		add(MineraculousBlockTags.CATACLYSM_IMMUNE, "Cataclysm Immune");

		add(MineraculousEntityEvents.STEALING_WARNING_KEY, "You may not rest now, there are thieves nearby.");
		add(ExternalInventoryScreen.ITEM_BOUND_KEY, "This item is bound to the player.");
	}

	private void addConfigs()
	{
		addConfigTitle(Mineraculous.MOD_NAME);

		addConfigCategory("miraculous", "Miraculous");
		addConfig("miraculous_comment", MineraculousClientConfig.miraculous_comment);
		addConfig("enable_per_player_customization_comment", MineraculousClientConfig.enable_per_player_customization_comment);
		addConfig("enablePerPlayerCustomization", "Enable Per Player Customization");

		addConfigCategory("stealing", "Item Stealing");
		addConfig("stealing_comment", MineraculousServerConfig.stealing_comment);
		addConfig("stealing_duration_comment", MineraculousServerConfig.stealing_duration_comment);
		addConfig("stealingDuration", "Stealing Duration");
		addConfig("enable_universal_stealing_comment", MineraculousServerConfig.enable_universal_stealing_comment);
		addConfig("enableUniversalStealing", "Enable Universal Stealing");
		addConfig("enable_sleep_stealing_comment", MineraculousServerConfig.enable_sleep_stealing_comment);
		addConfig("enableSleepStealing", "Enable Sleep Stealing");
		addConfig("wake_up_chance_comment", MineraculousServerConfig.wake_up_chance_comment);
		addConfig("wakeUpChance", "Wake Up Chance");
	}

	protected void add(MiraculousType type, String name)
	{
		add(type.getTranslationKey(), name);
	}

	protected void cheese(String name, Map<CheeseBlock.Age, DeferredItem<?>> wedges, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxedBlocks)
	{
		for (CheeseBlock.Age age: CheeseBlock.Age.values()) {
			add(wedges.get(age).get(), WordUtils.capitalize(age.getSerializedName()).replace('_','-') + " Wedge of " + name);
			add(blocks.get(age).get(), WordUtils.capitalize(age.getSerializedName()).replace('_','-') + " Block of " + name);
			add(waxedBlocks.get(age).get(), "Waxed " + WordUtils.capitalize(age.getSerializedName()).replace('_','-') + " Block of " + name);
		}
	}
}
