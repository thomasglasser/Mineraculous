package dev.thomasglasser.mineraculous.data.lang;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.kamiko.categories.TargetPlayerMenuCategory;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationChatScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.LookCustomizationScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.MiraculousEligiblePlayerEntry;
import dev.thomasglasser.mineraculous.client.gui.screens.MiraculousTransferScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.entity.decoration.MineraculousPaintingVariants;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerProfessions;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedEnUsLanguageProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.text.WordUtils;

public class MineraculousEnUsLanguageProvider extends ExtendedEnUsLanguageProvider {
    public MineraculousEnUsLanguageProvider(PackOutput output) {
        super(output, Mineraculous.MOD_ID);
    }

    public MineraculousEnUsLanguageProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void addTranslations() {
        add(MineraculousItems.LADYBUG_YOYO.get(), "Ladybug Yoyo");
        add(MineraculousItems.CAT_STAFF.get(), "Cat Staff");
        add(MineraculousItems.BUTTERFLY_CANE.get(), "Butterfly Cane");
        add(MineraculousItems.MIRACULOUS.get(), "Miraculous");
        add(MineraculousItems.KAMIKO_SPAWN_EGG.get(), "Kamiko Spawn Egg");
        add(MineraculousItems.CATACLYSM_DUST.get(), "Cataclysm Dust");
        add(MineraculousBlocks.HIBISCUS_BUSH.asItem(), "Hibiscus");

        addArmorTrim(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Ladybug");
        addArmorTrim(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Cat");
        addArmorTrim(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE.get(), "Butterfly");

        add(MineraculousCreativeModeTabs.MIRACULOUS.get(), "Miraculous");
        add(MineraculousCreativeModeTabs.KAMIKOTIZATION_TOOLS.get(), "Kamikotization Tools");
        add(MineraculousCreativeModeTabs.KAMIKOTIZABLES.get(), "Kamikotizables");
        add(MineraculousCreativeModeTabs.MINERACULOUS.get(), "Mineraculous");

        add(MineraculousArmors.MIRACULOUS.HEAD.get(), "Miraculous Mask");
        add(MineraculousArmors.MIRACULOUS.CHEST.get(), "Miraculous Chestplate");
        add(MineraculousArmors.MIRACULOUS.LEGS.get(), "Miraculous Leggings");
        add(MineraculousArmors.MIRACULOUS.FEET.get(), "Miraculous Boots");
        add(MineraculousArmors.KAMIKOTIZATION.HEAD.get(), "Kamikotization Mask");
        add(MineraculousArmors.KAMIKOTIZATION.CHEST.get(), "Kamikotization Chestplate");
        add(MineraculousArmors.KAMIKOTIZATION.LEGS.get(), "Kamikotization Leggings");
        add(MineraculousArmors.KAMIKOTIZATION.FEET.get(), "Kamikotization Boots");

        add(MineraculousBlocks.CATACLYSM_BLOCK.get(), "Block of Cataclysm");
        add(MineraculousBlocks.CHEESE_POT.get(), "Cheese Pot");
        add(MineraculousBlocks.HIBISCUS_BUSH.get(), "Hibiscus Bush");

        cheese("Cheese", MineraculousItems.CHEESE_WEDGES, MineraculousBlocks.CHEESE_BLOCKS, MineraculousItems.WAXED_CHEESE_WEDGES, MineraculousBlocks.WAXED_CHEESE_BLOCKS);
        cheese("Camembert", MineraculousItems.CAMEMBERT_WEDGES, MineraculousBlocks.CAMEMBERT_BLOCKS, MineraculousItems.WAXED_CAMEMBERT_WEDGES, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS);

        addConfigs();

        add(MineraculousEntityTypes.KAMIKO.get(), "Kamiko");
        add(MineraculousEntityTypes.KWAMI.get(), "Kwami");
        add(MineraculousEntityTypes.LUCKY_CHARM_ITEM_SPAWNER.get(), "Lucky Charm Item Spawner");
        add(MineraculousEntityTypes.THROWN_LADYBUG_YOYO.get(), "Ladybug Yoyo");
        add(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), "Cat Staff");
        add(MineraculousEntityTypes.THROWN_BUTTERFLY_CANE.get(), "Butterfly Cane");

        add(MineraculousKeyMappings.MIRACULOUS_CATEGORY, "Miraculous");
        add(MineraculousKeyMappings.TRANSFORM.get(), "Transform");
        add(MineraculousKeyMappings.ACTIVATE_POWER.get(), "Activate Power/Renounce Miraculous");
        add(MineraculousKeyMappings.ACTIVATE_TOOL.get(), "Activate Tool");
        add(MineraculousKeyMappings.OPEN_TOOL_WHEEL.get(), "Equip/Return Tool/Open Tool Wheel");
        add(MineraculousKeyMappings.TAKE_BREAK_ITEM.get(), "Take/Break Item");
        add(MineraculousKeyMappings.UNWIND_YOYO.get(), "Unwind YoYo");
        add(MineraculousKeyMappings.WIND_YOYO.get(), "Wind YoYo");

        add(MiraculousData.NAME_NOT_SET, "You haven't set your %s hero name yet! Use /miraculous %s name <name> to set.");

        add(MiraculousCommand.NOT_SET, "not set");
        add(MiraculousCommand.NAME_QUERY_SUCCESS_SELF, "Your %s hero name is %s.");
        add(MiraculousCommand.NAME_QUERY_SUCCESS_OTHER, "%s's %s hero name is %s.");
        add(MiraculousCommand.NAME_SET_SUCCESS_SELF, "Your %s hero name has been set to %s.");
        add(MiraculousCommand.NAME_SET_SUCCESS_OTHER, "Set %s's %s hero name to %s.");
        add(MiraculousCommand.NAME_CLEAR_SUCCESS_SELF, "Your %s hero name has been cleared.");
        add(MiraculousCommand.NAME_CLEAR_SUCCESS_OTHER, "Cleared %s's %s hero name.");
        add(MiraculousCommand.CUSTOMIZE_OPEN_SUCCESS_SELF, "Opening %s miraculous look customization screen.");
        add(MiraculousCommand.CUSTOMIZE_OPEN_SUCCESS_OTHER, "Opening %s miraculous look customization screen for %s.");
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
        add(MiraculousCommand.CUSTOM_LOOKS_NOT_ENABLED, "The server has not enabled custom looks.");
        add(MiraculousCommand.CUSTOM_LOOKS_NO_NUMBERS, "Custom looks must not contain numbers.");
        add(MiraculousCommand.CUSTOM_LOOKS_NO_GLOWMASK, "Custom looks must not contain 'glowmask'.");
        add(MiraculousCommand.MIRACULOUS_INVALID, "Invalid miraculous type %s");

        addMiraculous(MineraculousMiraculous.BUTTERFLY, "Butterfly");
        addMiraculous(MineraculousMiraculous.CAT, "Cat");
        addMiraculous(MineraculousMiraculous.LADYBUG, "Ladybug");

        addCapitalized(MineraculousAbilities.KAMIKOTIZATION);
        addCapitalized(MineraculousAbilities.KAMIKO_CONTROL);
        addCapitalized(MineraculousAbilities.KAMIKOTIZED_COMMUNICATION);
        addCapitalized(MineraculousAbilities.CATACLYSM);
        addCapitalized(MineraculousAbilities.CAT_VISION);
        addCapitalized(MineraculousAbilities.MIRACULOUS_LADYBUG);

        add(MineraculousItemTags.BUTTERFLY_KWAMI_FOODS, "Butterfly Kwami Foods");
        add(MineraculousItemTags.BUTTERFLY_KWAMI_TREATS, "Butterfly Kwami Treats");
        add(MineraculousItemTags.CAT_KWAMI_FOODS, "Cat Kwami Foods");
        add(MineraculousItemTags.CAT_KWAMI_TREATS, "Cat Kwami Treats");
        add(MineraculousItemTags.LADYBUG_KWAMI_FOODS, "Ladybug Kwami Foods");
        add(MineraculousItemTags.LADYBUG_KWAMI_TREATS, "Ladybug Kwami Treats");
        add(MineraculousItemTags.CATACLYSM_IMMUNE, "Cataclysm Immune");
        add(MineraculousItemTags.CHEESES_FOODS, "Cheeses");
        add(MineraculousItemTags.CHEESE, "Cheese");
        add(MineraculousItemTags.CAMEMBERT, "Camembert");

        addProfession(MineraculousVillagerProfessions.FROMAGER, "Fromager");

        add(MineraculousBlockTags.CATACLYSM_IMMUNE, "Cataclysm Immune");

        add(ServerboundWakeUpPayload.STEALING_WARNING_KEY, "You may not rest now, there are thieves nearby.");
        add(ExternalInventoryScreen.ITEM_BOUND_KEY, "This item is bound to the player.");
        add(ServerboundTryBreakItemPayload.ITEM_UNBREAKABLE_KEY, "This item is unbreakable by normal means.");
        add(MineraculousEntityEvents.ITEM_BROKEN_KEY, "Broken");

        add(TargetPlayerMenuCategory.TARGET_PROMPT, "Select a player to target");

        add(Kamiko.CANT_KAMIKOTIZE_TRANSFORMED, "Kamikotizing transformed players is not currently supported.");

        add(Kamikotization.NO_KAMIKOTIZATIONS, "No Kamikotizations found in world, have you installed any addons?");

        add(MineraculousClientUtils.CHOOSE, "Choose");
        add(MineraculousClientUtils.NAME, "Name");

        add(LookCustomizationScreen.TITLE, "%s Look Customization");

        add(KamikotizationSelectionScreen.TITLE, "Kamikotization");
        add(KamikotizationSelectionScreen.NO_KAMIKOTIZATIONS, "No valid kamikotizations found for %s");
        add(KamikotizationSelectionScreen.TOOL, "Tool:");
        add(KamikotizationSelectionScreen.ACTIVE_ABILITY, "Active Ability:");
        add(KamikotizationSelectionScreen.PASSIVE_ABILITIES, "Passive Abilities:");

        add(KamikotizationChatScreen.INTRO_NAME, "%s, I am %s.");
        add(KamikotizationChatScreen.INTRO_NAMELESS, "%s.");
        add(KamikotizationChatScreen.ACCEPT, "Accept Kamikotization");

        add(MineraculousClientEvents.REVOKE, "Revoke Kamikotization");
        add(MineraculousClientEvents.REVOKE_WITH_SPACE, "Revoke Kamikotization (Press Space)");

        add(MiraculousTransferScreen.TITLE, "Miraculous Transfer");
        add(MiraculousEligiblePlayerEntry.RENOUNCE, "Renounce");
        add(MiraculousEligiblePlayerEntry.TRANSFER, "Transfer");

        addAttackWithPlayer(MineraculousDamageTypes.CATACLYSM, "%1$s crumbled to dust", "while fighting %2$s");

        add(MineraculousMobEffects.CATACLYSMED.get(), "Cataclysmed");

        Arrays.stream(LadybugYoyoItem.Ability.values()).toList().forEach(ability -> add(ability.translationKey(), capitalize(ability.name())));
        Arrays.stream(CatStaffItem.Ability.values()).toList().forEach(ability -> add(ability.translationKey(), capitalize(ability.name())));
        Arrays.stream(ButterflyCaneItem.Ability.values()).toList().forEach(ability -> add(ability.translationKey(), capitalize(ability.name())));

        addCuriosSlot(MineraculousCuriosProvider.SLOT_BROOCH);
        addCuriosSlot(MineraculousCuriosProvider.SLOT_EARRINGS);

        add(MineraculousSoundEvents.CATACLYSM_ACTIVATE.get(), "Cataclysm activates");
        add(MineraculousSoundEvents.CATACLYSM_USE.get(), "Cataclysm triggers");
        add(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE.get(), "Kamiko powers up");
        add(MineraculousSoundEvents.KAMIKOTIZATION_USE.get(), "Kamikotization begins");
        add(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE.get(), "Lucky Charm activates");
        add(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE.get(), "Miraculous Ladybugs flourish");
        add(MineraculousSoundEvents.GENERIC_SHIELD.get(), "Weapon whirs");
        add(MineraculousSoundEvents.LADYBUG_YOYO_SHIELD.get(), "Yoyo whirs");
        add(MineraculousSoundEvents.CAT_STAFF_EXTEND.get(), "Staff extends");
        add(MineraculousSoundEvents.CAT_STAFF_RETRACT.get(), "Staff retracts");
        add(MineraculousSoundEvents.GENERIC_TRANSFORM.get(), "Miraculous holder transforms");
        add(MineraculousSoundEvents.GENERIC_DETRANSFORM.get(), "Miraculous holder detransforms");
        add(MineraculousSoundEvents.GENERIC_TIMER_BEEP.get(), "Miraculous beeps");
        add(MineraculousSoundEvents.GENERIC_TIMER_END.get(), "Miraculous powers down");
        add(MineraculousSoundEvents.LADYBUG_TRANSFORM.get(), "Ladybug Miraculous holder puts spots on");
        add(MineraculousSoundEvents.CAT_TRANSFORM.get(), "Cat Miraculous holder takes claws out");
        add(MineraculousSoundEvents.BUTTERFLY_TRANSFORM.get(), "Butterfly Miraculous holder raises wings");
        add(MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM.get(), "Kamikotized player transforms");
        add(MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM.get(), "Kamikotized player detransforms");
        add(MineraculousSoundEvents.KWAMI_HUNGRY.get(), "Kwami hungers");
        add(MineraculousSoundEvents.KWAMI_SUMMON.get(), "Kwami appears");

        addPaintingVariant(MineraculousPaintingVariants.LADYBUG, "Ladybug", "NastyaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_LADYBUG, "Mini Ladybug", "NastyaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.CAT, "Cat", "NastyaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_CAT, "Mini Cat", "NastyaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.BUTTERFLY, "Butterfly", "NastyaGalaxy");
        addPaintingVariant(MineraculousPaintingVariants.MINI_BUTTERFLY, "Mini Butterfly", "NastyaGalaxy");

        add(MineraculousPacks.AKUMATIZATION, "Akumatization Pack", "Renames \"Kamikotization\" to \"Akumatization\"");
    }

    private void addConfigs() {
        addConfigTitle(Mineraculous.MOD_NAME);

        // Server
        addConfigSection(MineraculousServerConfig.MIRACULOUS, "Miraculous", "Settings for miraculous");
        addConfig(MineraculousServerConfig.get().enableCustomization, "Enable Customization", "Enable customization of miraculous suits and items. §4WARNING: This may lead to vulnerabilities. Only enable if you trust server members.");
        addConfig(MineraculousServerConfig.get().customizationPermissionsMode, "Customization Permissions Mode", "Permissions mode for customization. Whitelist: Only whitelisted players can customize. Blacklist: Only non-blacklisted players can customize.");
        addConfig(MineraculousServerConfig.get().enableMiraculousTimer, "Enable Miraculous Timer", "Enable the detransformation timer for miraculous holders before they reach adulthood");
        addConfig(MineraculousServerConfig.get().enableLimitedPower, "Enable Limited Power", "Enable limited power for miraculous holders before they reach adulthood");
        addConfig(MineraculousServerConfig.get().enableKamikotizationRejection, "Enable Kamikotization Rejection", "Enable rejection of kamikotization by the victim");

        addConfigSection(MineraculousServerConfig.STEALING, "Stealing", "Settings for item stealing");
        addConfig(MineraculousServerConfig.get().stealingDuration, "Stealing Duration", "Duration in seconds that the key must be held to steal an item");
        addConfig(MineraculousServerConfig.get().enableUniversalStealing, "Enable Universal Stealing", "Enable item stealing from all players all the time");
        addConfig(MineraculousServerConfig.get().enableSleepStealing, "Enable Sleep Stealing", "Enable item stealing from players while they sleep");
        addConfig(MineraculousServerConfig.get().wakeUpChance, "Wake Up Chance", "Percent chance that a player will wake up while being stolen from");

        // Client
        addConfigSection(MineraculousClientConfig.COSMETICS, "Player Cosmetics", "Settings for player cosmetics");
        addConfig(MineraculousClientConfig.get().displaySnapshotTesterCosmetic, "Display Snapshot Tester Cosmetic", "Display your preferred Snapshot Tester Cosmetic (if eligible)");
//        addConfig(MineraculousClientConfig.get().snapshotTesterCosmeticChoice, "Snapshot Tester Cosmetic Choice", "The Snapshot Tester Cosmetic to be displayed (if eligible)");
        addConfig(MineraculousClientConfig.get().displayDevTeamCosmetic, "Display Dev Team Cosmetic", "Display the Dev Team cosmetic (if eligible)");
        addConfig(MineraculousClientConfig.get().displayLegacyDevTeamCosmetic, "Display Legacy Dev Team Cosmetic", "Display the Legacy Dev Team cosmetic (if eligible)");

        addConfigSection(MineraculousClientConfig.TOOL_WHEEL, "Tool Wheel", "Settings for the tool wheel");
        addConfig(MineraculousClientConfig.get().animationSpeed, "Animation Speed", "The speed at which the tool wheel opens");
    }

    protected void cheese(String name, Map<CheeseBlock.Age, DeferredItem<?>> wedges, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, Map<CheeseBlock.Age, DeferredItem<?>> waxedWedges, Map<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxedBlocks) {
        for (CheeseBlock.Age age : CheeseBlock.Age.values()) {
            add(wedges.get(age).get(), capitalize(age.getSerializedName()).replace('_', '-') + " Wedge of " + name);
            add(blocks.get(age).get(), capitalize(age.getSerializedName()).replace('_', '-') + " Block of " + name);
            add(waxedWedges.get(age).get(), "Waxed " + capitalize(age.getSerializedName()).replace('_', '-') + " Wedge of " + name);
            add(waxedBlocks.get(age).get(), "Waxed " + capitalize(age.getSerializedName()).replace('_', '-') + " Block of " + name);
        }
    }

    protected void addMiraculous(ResourceKey<Miraculous> type, String name) {
        add(type, name);
        add(MineraculousEntityTypes.KWAMI.getId().toLanguageKey("entity", type.location().toShortLanguageKey()), name + " Kwami");
    }

    protected void add(RadialMenuOption option, String name) {
        add(option.translationKey(), name);
    }

    protected void addCuriosSlot(String name) {
        add("curios.identifier." + name, capitalize(name));
    }

    protected String capitalize(String name) {
        return WordUtils.capitalize(name.toLowerCase().replace('_', ' '));
    }

    protected void addCapitalized(ResourceKey<?> key) {
        add(key, capitalize(key.location().getPath()));
    }
}
