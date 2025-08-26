package dev.thomasglasser.mineraculous.impl.data.lang;

import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.api.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.kamiko.categories.KamikoTargetPlayerMenuCategory;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.MiraculousEligiblePlayerEntry;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.MiraculousTransferScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.PerformerKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.ReceiverKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.impl.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.impl.world.item.armortrim.MineraculousTrimPatterns;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedEnUsLanguageProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemNameBlockItem;

public class MineraculousEnUsLanguageProvider extends ExtendedEnUsLanguageProvider {
    public MineraculousEnUsLanguageProvider(PackOutput output) {
        super(output, Mineraculous.MOD_ID);
    }

    protected MineraculousEnUsLanguageProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addBlocks();
        addEntityTypes();
        addTabs();
        addKeyMappings();
        addCommands();
        addMiraculouses();
        addAbilities();
        addProfessions();
        addGuis();
        addDamageTypes();
        addMobEffects();
        addCuriosSlots();
        addSoundEvents();
        addPaintingVariants();
        addArmorTrims();
        addTags();
        addPacks();
        addConfigs();
    }

    protected void addSuitArmor(ArmorSet set, String displayName) {
        add(set, displayName, "Mask", "Chestplate", "Leggings", "Boots");
    }

    protected void cheese(Map<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> wedges, Map<AgeingCheese.Age, DeferredItem<ItemNameBlockItem>> waxedWedges, Map<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> blocks, Map<AgeingCheese.Age, DeferredBlock<PieceBlock>> waxedBlocks, String name) {
        for (AgeingCheese.Age age : AgeingCheese.Age.values()) {
            String ageName = capitalize(age.getSerializedName()).replace('_', '-');
            add(wedges.get(age).get(), "Wedge of " + ageName + " " + name);
            add(waxedWedges.get(age).get(), "Waxed Wedge of " + ageName + " " + name);
            add(blocks.get(age).get(), "Block of " + ageName + " " + name);
            add(waxedBlocks.get(age).get(), "Waxed Block of " + ageName + " " + name);
        }
    }

    protected void add(RadialMenuOption option, String name) {
        add(option.displayName(), name);
    }

    protected <T extends Enum<T> & RadialMenuOption> void addToolAbilities(T[] options) {
        for (T option : options) {
            add(option.displayName(), capitalize(option.name()));
        }
    }

    private void addItems() {
        add(MineraculousItems.LADYBUG_YOYO.get(), "Ladybug Yoyo");
        add(MineraculousItems.CAT_STAFF.get(), "Cat Staff");
        add(MineraculousItems.BUTTERFLY_CANE.get(), "Butterfly Cane");
        add(MineraculousItems.MIRACULOUS.get(), "Miraculous");
        add(MineraculousItems.GREAT_SWORD.get(), "Great Sword");
        add(MineraculousItems.CATACLYSM_DUST.get(), "Cataclysm Dust");
        add(MineraculousBlocks.HIBISCUS_BUSH.asItem(), "Hibiscus");

        // Tool Abilities
        addToolAbilities(LadybugYoyoItem.Ability.values());
        addToolAbilities(CatStaffItem.Ability.values());
        addToolAbilities(ButterflyCaneItem.Ability.values());

        // Armor
        addSuitArmor(MineraculousArmors.MIRACULOUS, "Miraculous");
        addSuitArmor(MineraculousArmors.KAMIKOTIZATION, "Kamikotization");
    }

    private void addBlocks() {
        add(MineraculousBlocks.CATACLYSM_BLOCK.get(), "Block of Cataclysm");
        add(MineraculousBlocks.CHEESE_POT.get(), "Cheese Pot");
        add(MineraculousBlocks.HIBISCUS_BUSH.get(), "Hibiscus Bush");

        cheese(MineraculousItems.CHEESE, MineraculousItems.WAXED_CHEESE, MineraculousBlocks.CHEESE, MineraculousBlocks.WAXED_CHEESE, "Cheese");
        cheese(MineraculousItems.CAMEMBERT, MineraculousItems.WAXED_CAMEMBERT, MineraculousBlocks.CAMEMBERT, MineraculousBlocks.WAXED_CAMEMBERT, "Camembert");
    }

    private void addEntityTypes() {
        add(MineraculousEntityTypes.KAMIKO.get(), MineraculousItems.KAMIKO_SPAWN_EGG.get(), "Kamiko");

        add(MineraculousEntityTypes.KWAMI.get(), "Kwami");
        add(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), "Lucky Charm Item Spawner");
        add(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), "Ladybug Yoyo");
        add(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), "Cat Staff");
        add(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), "Butterfly Cane");
    }

    private void addTabs() {
        add(MineraculousCreativeModeTabs.MIRACULOUS.get(), "Miraculous");
        add(MineraculousCreativeModeTabs.KAMIKOTIZATION_TOOLS.get(), "Kamikotization Tools");
        add(MineraculousCreativeModeTabs.KAMIKOTIZABLES.get(), "Kamikotizables");
        add(MineraculousCreativeModeTabs.MINERACULOUS.get(), "Mineraculous");
    }

    private void addKeyMappings() {
        add(MineraculousKeyMappings.MIRACULOUS_CATEGORY, "Miraculous");
        add(MineraculousKeyMappings.TRANSFORM, "Transform");
        add(MineraculousKeyMappings.ACTIVATE_POWER, "Activate Power/Renounce Miraculous");
        add(MineraculousKeyMappings.TOGGLE_ACTIVE, "De/Activate Item");
        add(MineraculousKeyMappings.OPEN_ITEM_RADIAL_MENU, "Open Item Radial Menu");
        add(MineraculousKeyMappings.TAKE_BREAK_ITEM, "Take/Break Item");
        add(MineraculousKeyMappings.DESCEND_TOOL, "Descend Tool");
        add(MineraculousKeyMappings.ASCEND_TOOL, "Ascend Tool");
    }

    private void addCommands() {
        // Miraculous
        add(MiraculousData.NAME_NOT_SET, "You haven't set your %s hero name yet! Use '/miraculous %s customize' to set.");
        // Customize
        add(MiraculousCommand.CUSTOMIZE_OPEN_SUCCESS_SELF, "Opening %s miraculous look customization screen.");
        add(MiraculousCommand.CUSTOMIZE_OPEN_SUCCESS_OTHER, "Opening %s miraculous look customization screen for %s.");
        // Charged
        add(MiraculousCommand.CHARGED_TRUE, "charged");
        add(MiraculousCommand.CHARGED_FALSE, "not charged");
        add(MiraculousCommand.CHARGED_QUERY_SUCCESS_SELF, "Your %s kwami is %s.");
        add(MiraculousCommand.CHARGED_QUERY_SUCCESS_OTHER, "%s's %s kwami is %s.");
        add(MiraculousCommand.CHARGED_SET_SUCCESS_SELF, "Set charged state of %s kwami to %s");
        add(MiraculousCommand.CHARGED_SET_SUCCESS_OTHER, "Set charged state of %s's %s kwami to %s");
        add(MiraculousCommand.CHARGED_FAILURE_TRANSFORMED, "This command cannot be executed while %s is transformed.");
        add(MiraculousCommand.CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF, "%s kwami not found in the world");
        add(MiraculousCommand.CHARGED_FAILURE_KWAMI_NOT_FOUND_OTHER, "%s's %s kwami not found in the world");
        // Power Level
        add(MiraculousCommand.POWER_LEVEL_QUERY_SUCCESS_SELF, "Your %s power level is %s.");
        add(MiraculousCommand.POWER_LEVEL_QUERY_SUCCESS_OTHER, "%s's %s power level is %s.");
        add(MiraculousCommand.POWER_LEVEL_SET_SUCCESS_SELF, "Your %s power level has been set to %s.");
        add(MiraculousCommand.POWER_LEVEL_SET_SUCCESS_OTHER, "Set %s's %s power level to %s.");
        // Exceptions
        add(MiraculousCommand.EXCEPTION_MIRACULOUS_INVALID, "Invalid miraculous type %s");
    }

    private void addMiraculouses() {
        add(Miraculouses.BUTTERFLY, "Butterfly");
        add(Miraculouses.CAT, "Cat");
        add(Miraculouses.LADYBUG, "Ladybug");
    }

    private void addAbilities() {
        addCapitalized(Abilities.KAMIKOTIZATION);
        addCapitalized(Abilities.KAMIKO_CONTROL);
        addCapitalized(Abilities.KAMIKOTIZED_COMMUNICATION);
        addCapitalized(Abilities.CATACLYSM);
        addCapitalized(Abilities.CAT_VISION);
        addCapitalized(Abilities.MIRACULOUS_LADYBUG);
    }

    private void addProfessions() {
        addProfession(MineraculousVillagerProfessions.FROMAGER, "Fromager");
    }

    private void addGuis() {
        // General
        add(MineraculousClientUtils.GUI_CHOOSE, "Choose");
        add(MineraculousClientUtils.GUI_NAME, "Name");

        // Taking/Breaking
        add(ServerboundWakeUpPayload.STEALING_WARNING, "You may not rest now, there are thieves nearby.");
        add(ExternalInventoryScreen.ITEM_BOUND_KEY, "This item is bound to the player.");
        add(MineraculousItemUtils.ITEM_UNBREAKABLE_KEY, "This item is unbreakable by normal means.");

        // Kamiko Gui
        add(KamikoTargetPlayerMenuCategory.TARGET_PROMPT, "Select a player to target");
        add(Kamiko.CANT_KAMIKOTIZE_TRANSFORMED, "Kamikotizing transformed players is not currently supported.");
        add(Kamikotization.NO_KAMIKOTIZATIONS, "No Kamikotizations found in world, have you installed any addons?");

        // Kamikotization View
        add(MineraculousGuis.REVOKE, "Revoke Kamikotization");
        add(MineraculousGuis.PRESS_ENTER, "(Press Enter)");

        // Kamikotization Selection Screen
        add(KamikotizationSelectionScreen.TITLE, "Kamikotization");
        add(KamikotizationSelectionScreen.NO_KAMIKOTIZATIONS, "No valid kamikotizations found for %s");
        add(KamikotizationSelectionScreen.TOOL, "Tool:");
        add(KamikotizationSelectionScreen.ACTIVE_ABILITY, "Active Ability:");
        add(KamikotizationSelectionScreen.PASSIVE_ABILITIES, "Passive Abilities:");

        // Performer Kamikotization Chat Screen
        add(PerformerKamikotizationChatScreen.INTRO_NAME, "%s, I am %s.");
        add(PerformerKamikotizationChatScreen.INTRO_NAMELESS, "%s.");

        // Receiver Kamikotization Chat Screen
        add(ReceiverKamikotizationChatScreen.ACCEPT, "Accept Kamikotization");

        // Miraculous Transfer Screen
        add(MiraculousTransferScreen.TITLE, "Miraculous Transfer");
        add(MiraculousEligiblePlayerEntry.RENOUNCE, "Renounce");
        add(MiraculousEligiblePlayerEntry.TRANSFER, "Transfer");
    }

    private void addDamageTypes() {
        // Cataclysm
        addAttackWithPlayer(MineraculousDamageTypes.CATACLYSM, "%1$s crumbled to dust", "while fighting %2$s");
    }

    private void addMobEffects() {
        add(MineraculousMobEffects.CATACLYSM.get(), "Cataclysm");
    }

    private void addCuriosSlots() {
        addCuriosSlot(MineraculousCuriosProvider.SLOT_BROOCH);
        addCuriosSlot(MineraculousCuriosProvider.SLOT_EARRINGS);
    }

    private void addSoundEvents() {
        // Abilities
        add(MineraculousSoundEvents.CATACLYSM_ACTIVATE.get(), "Cataclysm activates");
        add(MineraculousSoundEvents.CATACLYSM_USE.get(), "Cataclysm triggers");
        add(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE.get(), "Kamiko powers up");
        add(MineraculousSoundEvents.KAMIKOTIZED_COMMUNICATION_ACTIVATE.get(), "Kamikotization begins");
        add(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE.get(), "Lucky Charm activates");
        add(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE.get(), "Miraculous Ladybugs flourish");

        // Miraculous Tools
        add(MineraculousSoundEvents.GENERIC_SPIN.get(), "Weapon whirs");
        add(MineraculousSoundEvents.LADYBUG_YOYO_SPIN.get(), "Yoyo whirs");
        add(MineraculousSoundEvents.CAT_STAFF_EXTEND.get(), "Staff extends");
        add(MineraculousSoundEvents.CAT_STAFF_RETRACT.get(), "Staff retracts");

        // Miraculous
        add(MineraculousSoundEvents.GENERIC_TRANSFORM.get(), "Miraculous holder transforms");
        add(MineraculousSoundEvents.GENERIC_DETRANSFORM.get(), "Miraculous holder detransforms");
        add(MineraculousSoundEvents.GENERIC_TIMER_WARNING.get(), "Miraculous beeps");
        add(MineraculousSoundEvents.GENERIC_TIMER_END.get(), "Miraculous powers down");
        add(MineraculousSoundEvents.LADYBUG_TRANSFORM.get(), "Ladybug Miraculous holder puts spots on");
        add(MineraculousSoundEvents.CAT_TRANSFORM.get(), "Cat Miraculous holder takes claws out");
        add(MineraculousSoundEvents.BUTTERFLY_TRANSFORM.get(), "Butterfly Miraculous holder raises wings");

        // Kamikotization
        add(MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM.get(), "Kamikotized player transforms");
        add(MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM.get(), "Kamikotized player detransforms");

        // Kwami
        add(MineraculousSoundEvents.KWAMI_HURT.get(), "Kwami hungers");
        add(MineraculousSoundEvents.KWAMI_SUMMON.get(), "Kwami appears");
    }

    private void addPaintingVariants() {
        addPaintingVariant(MineraculousPaintingVariants.LADYBUG, "Ladybug", "NastiaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_LADYBUG, "Mini Ladybug", "NastiaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.CAT, "Cat", "NastiaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_CAT, "Mini Cat", "NastiaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.BUTTERFLY, "Butterfly", "NastiaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_BUTTERFLY, "Mini Butterfly", "NastiaGalaxy");
    }

    private void addArmorTrims() {
        addArmorTrim(MineraculousTrimPatterns.LADYBUG, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Ladybug");
        addArmorTrim(MineraculousTrimPatterns.CAT, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Cat");
        addArmorTrim(MineraculousTrimPatterns.BUTTERFLY, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Butterfly");
    }

    private void addTags() {
        // Block Tags
        add(MineraculousBlockTags.CATACLYSM_IMMUNE, MineraculousItemTags.CATACLYSM_IMMUNE, "Cataclysm Immune");
        add(MineraculousBlockTags.CHEESE_BLOCKS_FOODS, MineraculousItemTags.CHEESE_BLOCKS_FOODS, "Cheese Blocks");
        add(MineraculousBlockTags.CHEESE_BLOCKS, MineraculousItemTags.CHEESE_BLOCKS, "Cheese Blocks");
        add(MineraculousBlockTags.CAMEMBERT_BLOCKS, MineraculousItemTags.CAMEMBERT_BLOCKS, "Camembert Blocks");

        // Item Tags
        add(MineraculousItemTags.BUTTERFLY_KWAMI_FOODS, "Butterfly Kwami Foods");
        add(MineraculousItemTags.BUTTERFLY_KWAMI_TREATS, "Butterfly Kwami Treats");
        add(MineraculousItemTags.CAT_KWAMI_FOODS, "Cat Kwami Foods");
        add(MineraculousItemTags.CAT_KWAMI_TREATS, "Cat Kwami Treats");
        add(MineraculousItemTags.LADYBUG_KWAMI_FOODS, "Ladybug Kwami Foods");
        add(MineraculousItemTags.LADYBUG_KWAMI_TREATS, "Ladybug Kwami Treats");
        add(MineraculousItemTags.CHEESES_FOODS, "Cheeses");
        add(MineraculousItemTags.CHEESE, "Cheese");
        add(MineraculousItemTags.CAMEMBERT, "Camembert");
    }

    private void addPacks() {
        add(MineraculousPacks.AKUMATIZATION, "Akumatization Pack", "Renames \"Kamikotization\" to \"Akumatization\"");
    }

    private void addConfigs() {
        addConfigTitle(Mineraculous.MOD_NAME);

        // Server
        addConfigSection(MineraculousServerConfig.MIRACULOUS, "Miraculous", "Settings for miraculous");
        addConfig(MineraculousServerConfig.get().enableCustomization, "Enable Customization", "Enable customization of miraculous suits and items. §4WARNING: This may lead to vulnerabilities. Only enable if you trust server members.");
        addConfig(MineraculousServerConfig.get().customizationPermissionsMode, "Customization Permissions Mode", "Permissions mode for customization. Whitelist: Only whitelisted players can customize. Blacklist: Only non-blacklisted players can customize.");
        addConfig(MineraculousServerConfig.get().kwamiSummonTime, "Kwami Summon Time", "The amount of time (in seconds) that it takes for a kwami to summon");
        addConfig(MineraculousServerConfig.get().enableMiraculousTimer, "Enable Miraculous Timer", "Enable the detransformation timer for miraculous holders before they reach full maturity");
        addConfig(MineraculousServerConfig.get().miraculousTimerDuration, "Miraculous Timer Duration", "The amount of time (in seconds) between a miraculous holder using their power and detransforming");
        addConfig(MineraculousServerConfig.get().enableLimitedPower, "Enable Limited Power", "Enable limited power for miraculous holders before they reach adulthood");
        addConfig(MineraculousServerConfig.get().enableKamikotizationRejection, "Enable Kamikotization Rejection", "Enable rejection of kamikotization by the victim");
        addConfig(MineraculousServerConfig.get().luckyCharmSummonTimeMin, "Minimum Lucky Charm Summon Time", "The minimum amount of time (in seconds) that it takes for a lucky charm to be summoned");
        addConfig(MineraculousServerConfig.get().luckyCharmSummonTimeMax, "Maximum Lucky Charm Summon Time", "The maximum amount of time (in seconds) that it takes for a lucky charm to be summoned");
        addConfig(MineraculousServerConfig.get().maxToolLength, "Maximum Tool Length", "Amount of blocks that tools can be extended to");

        addConfigSection(MineraculousServerConfig.STEALING, "Stealing", "Settings for item stealing");
        addConfig(MineraculousServerConfig.get().stealingDuration, "Stealing Duration", "Duration in seconds that the key must be held to steal an item");
        addConfig(MineraculousServerConfig.get().enableUniversalStealing, "Enable Universal Stealing", "Enable item stealing from all players all the time");
        addConfig(MineraculousServerConfig.get().enableSleepStealing, "Enable Sleep Stealing", "Enable item stealing from players while they sleep");
        addConfig(MineraculousServerConfig.get().wakeUpChance, "Wake Up Chance", "Percent chance that a player will wake up while being stolen from");

        // Client
        addConfigSection(MineraculousClientConfig.COSMETICS, "Player Cosmetics", "Settings for player cosmetics");
        addConfig(MineraculousClientConfig.get().displayBetaTesterCosmetic, "Display Beta Tester Cosmetic", "Display your preferred Beta Tester Cosmetic (if eligible)");
        addConfig(MineraculousClientConfig.get().betaTesterCosmeticChoice, "Beta Tester Cosmetic Choice", "The Beta Tester Cosmetic to be displayed (if eligible)");
        addConfig(MineraculousClientConfig.get().displayDevTeamCosmetic, "Display Dev Team Cosmetic", "Display the Dev Team cosmetic (if eligible)");
        addConfig(MineraculousClientConfig.get().displayLegacyDevTeamCosmetic, "Display Legacy Dev Team Cosmetic", "Display the Legacy Dev Team cosmetic (if eligible)");

        addConfigSection(MineraculousClientConfig.RADIAL_MENU, "Tool Wheel", "Settings for the tool wheel");
        addConfig(MineraculousClientConfig.get().animationSpeed, "Animation Speed", "The speed at which the tool wheel opens");
    }
}
