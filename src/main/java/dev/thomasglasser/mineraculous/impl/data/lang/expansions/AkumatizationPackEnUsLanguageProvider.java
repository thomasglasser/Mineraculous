package dev.thomasglasser.mineraculous.impl.data.lang.expansions;

import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationItemSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.ReceiverKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.tommylib.api.data.lang.EnUsOverrideLanguageProvider;
import net.minecraft.data.PackOutput;

public class AkumatizationPackEnUsLanguageProvider extends EnUsOverrideLanguageProvider {
    public AkumatizationPackEnUsLanguageProvider(PackOutput output) {
        super(output, MineraculousPacks.AKUMATIZATION.knownPack().id());
    }

    @Override
    protected void addTranslations() {
        addItems();
        addCreativeModeTabs();
        addEntityTypes();
        addAbilities();
        addGuis();
        addSounds();
        addConfigs();
        addAdvancements();
        addWiki();
    }

    @Override
    public String getName() {
        return "Akumatization Pack " + super.getName();
    }

    protected void add(RadialMenuOption option, String name) {
        add(option.displayName(), name);
    }

    private void addItems() {
        add(MineraculousArmors.KAMIKOTIZATION, "Akumatization", "Mask", "Chestplate", "Leggings", "Boots");

        add(ButterflyCaneItem.Mode.KAMIKO_STORE, "Akuma Store");
    }

    private void addCreativeModeTabs() {
        add(MineraculousCreativeModeTabs.KAMIKOTIZABLES.get(), "Akumatizables");
        add(MineraculousCreativeModeTabs.KAMIKOTIZATION_TOOLS.get(), "Akumatization Tools");
    }

    private void addEntityTypes() {
        add(MineraculousEntityTypes.KAMIKO.get(), "Akuma");
        add(MineraculousEntityTypes.KAMIKOTIZED_MINION.get(), "Akumatized Minion");
    }

    private void addAbilities() {
        add(Abilities.KAMIKOTIZATION, "Akumatization");
        add(Abilities.KAMIKO_CONTROL, "Akuma Control");
        add(Abilities.KAMIKOTIZED_COMMUNICATION, "Akumatized Communication");
    }

    private void addGuis() {
        // Kamiko Gui
        add(MineraculousGuis.REVOKE, "Revoke Akumatization");
        add(Kamiko.DETRANSFORM_TO_TRANSFORM, "Akumatization will begin when you detransform.");
        add(Kamiko.CANT_KAMIKOTIZE_TRANSFORMED, "Akumatizing transformed players is not currently supported.");
        add(Kamikotization.NO_KAMIKOTIZATIONS, "No Akumatizations found in world, have you installed any addons?");

        // Kamikotization Selection Screen
        add(KamikotizationSelectionScreen.TITLE, "Akumatization");
        add(KamikotizationItemSelectionScreen.NO_KAMIKOTIZATIONS, "No valid akumatizations found for %s");

        // Receiver Kamikotization Chat Screen
        add(ReceiverKamikotizationChatScreen.ACCEPT, "Accept Akumatization");

        // Keys
        add(MineraculousKeyMappings.REVOKE_KAMIKOTIZATION, "Revoke Akumatization");
    }

    private void addSounds() {
        // Abilities
        add(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE.get(), "Akuma powers up");
        add(MineraculousSoundEvents.KAMIKOTIZED_COMMUNICATION_ACTIVATE.get(), "Akumatization begins");

        // Kamikotization
        add(MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM.get(), "Akumatized player transforms");
        add(MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM.get(), "Akumatized player detransforms");
    }

    private void addConfigs() {
        addConfig(MineraculousServerConfig.get().enableKamikotizationRejection, "Enable Akumatization Rejection", "Enable rejection of akumatization by the victim");
        addConfig(MineraculousServerConfig.get().enableKamikoReplication, "Enable Akuma Replication", "Enable replication of akumas when left uncaptured");
        addConfig(MineraculousServerConfig.get().maxKamikoReplicas, "Maximum Akuma Replicas", "Maximum number of akuma replicas made by an akuma in one sitting");
        addConfig(MineraculousServerConfig.get().forceKamikotizeCreativePlayers, "Force Akumatize Creative Players", "Force akumatize players even in creative mode");
    }

    private void addAdvancements() {
        addAdvancement("miraculous", "kamikotize_butterfly", "desc", "Akumatize a butterfly");
        addAdvancement("miraculous", "kamikotize_butterfly", "title", "Fly Away My Little Akuma");
        addAdvancement("miraculous", "kamikotize_entity", "desc", "Provide power to another being via akumatization");
        addAdvancement("miraculous", "kamikotize_self", "desc", "Use the butterfly miraculous to akumatize yourself");
        addAdvancement("miraculous", "transform_kamikotization", "desc", "Accept an akumatization from the Butterfly miraculous holder");
    }

    private void addWiki() {
        // APIs
        // Abilities
        addEntryDescription("wiki", "apis", "abilities", "Supernatural abilities that can be used by Miraculous or Akumatization holders.");

        // Advancement Triggers
        addPageText("wiki", "apis", "advancement_triggers", "kamikotized_entity", "This trigger is called when a player akumatizes an entity.\nIt has four parameters:\n- \"player\": The player that akumatized the entity.\n- \"target\": The entity that was akumatized.\n- \"kamikotization\": The akumatization that was given to the entity.\n- \"self\": Whether the player akumatized themself.\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "kamikotized_entity", "Akumatized Entity");

        addPageText("wiki", "apis", "advancement_triggers", "performed_kamikotization_active_ability", "This trigger is called when a player performs an active ability provided by their akumatization.\nIt has three parameters:\n- \"player\": The player that performed the ability.\n- \"kamikotization\": The akumatization that provided the ability.\n- \"context\": The context in which the power was used.\nCan be any value for addon support,\nbut the ones included in the mod by default are:\n    - block\n    - entity\n    - living_entity\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "performed_kamikotization_active_ability", "Performed Akumatization Active Ability");

        addPageText("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "This trigger is called when a player is transformed by an akumatization.\nIt has three parameters:\n- \"player\": The player that was transformed.\n- \"kamikotization\": The akumatization that was used to transform.\n- \"self\": Whether the player akumatized themself.\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "Transformed Akumatization");

        // Data Maps
        addPageText("wiki", "apis", "data_maps", "lucky_charms", "Contextual lucky charms are determined for akumatizations, miraculous holders, and entities via data maps.\nThey are located in \"data/mineraculous/kamikotization/lucky_charms.json\",\n\"data/mineraculous/miraculous/lucky_charms.json\",\nand \"data/mineraculous/entity_type/lucky_charms.json\".\nGenerators for these can be found online [here](https://beta-jsons.thomasglasser.dev/partners/).\nGenerators for the loot table can be found [here](https://beta-jsons.thomasglasser.dev/loot-table/).\n*Note: At this time, to generate a lucky charm loot table, you must use a preset to set the \"type\" field to \"mineraculous:lucky_charm\".\nSearching the presets for \"lucky_charm\" will yield valid results.*\n");
        addPageText("wiki", "apis", "data_maps", "miraculous_effects", "Miraculous effects and attributes are provided to miraculous holders and akumatized entities while transformed.\nThese are determined via two data maps:\n- \"data/mineraculous/mob_effect/miraculous_effects.json\" for mob effects ([Generator](https://beta-jsons.thomasglasser.dev/mineraculous/data-map-miraculous-effects/))\n- \"data/mineraculous/attribute/miraculous_attribute_modifiers.json\" for attributes ([Generator](https://beta-jsons.thomasglasser.dev/mineraculous/data-map-miraculous-attribute-modifiers/))\n");

        // Kamikotizations (API)
        addEntryDescription("wiki", "apis", "kamikotizations", "Transformations that the Butterfly Miraculous can give to entities.");
        addPageText("wiki", "apis", "kamikotizations", "generator", "A generator for akumatizations can be found [here](https://beta-jsons.thomasglasser.dev/mineraculous/kamikotization/).\n");
        addPageText("wiki", "apis", "kamikotizations", "guide", "A guide for creating akumatizations can be found [here](https://beta-jsons.thomasglasser.dev/guides/kamikotization/).\n");
        addEntryName("wiki", "apis", "kamikotizations", "Akumatizations");

        // Tags
        addPageText("wiki", "apis", "tags", "damage_type", "There are two mod tags used by the mod:\n- \"mineraculous:hurts_kamikos\": Damage types that can damage Akumas.\n- \"mineraculous:is_cataclysm\": Damage types that can be considered cataclysm.\n");
        addPageText("wiki", "apis", "tags", "item",
                "There are seventeen mod tags and two common tags used by the mod:\n- \"mineraculous:kwami_preferred_foods/butterfly\": Items that can be used to have a better chance to charge the butterfly kwami.\n- \"mineraculous:kwami_treats/butterfly\": Items that can be used to immediately charge the butterfly kwami.\n- \"mineraculous:kwami_preferred_foods/cat\": Items that can be used to have a better chance to charge the cat kwami.\n- \"mineraculous:kwami_treats/cat\": Items that can be used to immediately charge the cat kwami.\n- \"mineraculous:kwami_preferred_foods/ladybug\": Items that can be used to have a better chance to charge the ladybug kwami.\n- \"mineraculous:kwami_treats/ladybug\": Items that can be used to immediately charge the ladybug kwami.\n- \"mineraculous:cheese\": Items that are normal cheese added by the mod.\n- \"mineraculous:cheese_blocks\": Items that are normal cheese blocks added by the mod.\n- \"mineraculous:camembert\": Items that are camembert cheese added by the mod.\n- \"mineraculous:camembert_blocks\": Items that are camembert cheese blocks added by the mod.\n- \"mineraculous:cataclysm_immune\": Items that cataclysm cannot apply to.\n- \"mineraculous:tough\": Items that take two tries to break if they do not have a max damage value.\n- \"mineraculous:lucky_charm_shader_immune\": Items that do not have a visual change when given as a lucky charm.\n- \"mineraculous:shooting_projectiles\": Projectiles that shoot down as a Lucky Charm instead of dropping normally.\n- \"mineraculous:generic_lucky_charms\": Lucky charm options when no specific pool is specified.\n- \"mineraculous:warden_distractors\": Items passed in a Warden lucky charm to distract it.\n- \"mineraculous:kamikotization_immune\": Items that cannot be used for Akumatization.\n- \"c:foods/cheeses\": Items from any mod that can be considered cheese and food.\n- \"c:foods/cheese_blocks\": An item copy of the \"c:foods/cheese_blocks\" block tag.\n");

        // Kamikotizations (Category)
        addCategoryDescription("wiki", "kamikotizations", "Powers given by the Butterfly Miraculous to normal players. Addons can inject entries to this category describing their akumatizations (see the Modonomicon Wiki for more info).");

        // General
        addEntryDescription("wiki", "kamikotizations", "kamikotizations_general", "Features of all akumatizations");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "receiving", "The only way to receive an akumatization is from an Akuma from the [Butterfly Miraculous](entry://miraculouses/butterfly).\nThe owner of the akuma sends it to a target to access the contents of the target's inventory.\nThey will then choose an akumatization to apply based on the items in the target's inventory.\nThe target has the option to accept the akumatizations via a button or can reject the akumatization with the Escape key if the server allows it.\n");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "rejecting", "Akumatizations can be rejected by the target if the server allows it by pressing the Reject Akumatization key (default: K).\nThis will release the akuma from the item and remove the akumatization and powers.\n");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "revoking", "Akumatizations are revoked when the akumatized item is destroyed or when the [Butterfly Miraculous](entry://miraculouses/butterfly) holder chooses to revoke the akumatization.\nThis will release the akuma from the item and remove the akumatization and powers.\n");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "using_tool", "When akumatized, the akumatized item will either be turned into a tool or the target will be given an ability to use on key press (default: O).\nThis tool and ability will be different depending on the akumatization;\nrefer to the [Akumatizations](category://kamikotizations) category for more info.\n");
        addCategoryName("wiki", "kamikotizations", "Akumatizations");

        // Miraculouses
        // Butterfly
        addPageText("wiki", "miraculouses", "butterfly", "abilities", "The Butterfly Miraculous has 3 abilities:\n- Akumatization\n- Akuma Control\n- Akumatized Communication\n");
        addPageText("wiki", "miraculouses", "butterfly", "blade", "Blade mode unsheathes a rapier-like blade that can be used to attack entities.\nIt can also be thrown, damaging any entity or item hit, and releasing an Akuma if inside.\n");
        addPageText("wiki", "miraculouses", "butterfly", "cane_abilities", "The tool of the Butterfly Miraculous is a Butterfly Cane.\nIt has five abilities:\n- Blade\n- Block\n- Akuma Store\n- Spyglass\n- Throw\n");

        addPageText("wiki", "miraculouses", "butterfly", "kamiko_control", "The Akuma Control ability can be activated by pressing the Activate Power button (default: Y) with an Akuma nearby.\nIt will cause a mask to appear on the performer's face and will allow seeing through the eyes of the Akuma.\nThe number keys then allow selecting a target that the Akuma will fly to.\nOnce it reaches the target, it will open the Akumatization Selection Screen and allow akumatizing the target.\n");
        addPageTitle("wiki", "miraculouses", "butterfly", "kamiko_control", "Akuma Control");

        addPageText("wiki", "miraculouses", "butterfly", "kamiko_store", "Akuma Store mode allows right clicking to store or release a single Akuma.\n");
        addPageTitle("wiki", "miraculouses", "butterfly", "kamiko_store", "Akuma Store");

        addPageText("wiki", "miraculouses", "butterfly", "kamikotization", "The Akumatization ability can be activated by pressing the Activate Power button (default: Y) with no Akumas nearby or stored.\nIt will cause particles to come from the performer's hand.\nIt will then convert any non-player entity into an Akuma on interaction.\n");
        addPageTitle("wiki", "miraculouses", "butterfly", "kamikotization", "Akumatization");

        addPageText("wiki", "miraculouses", "butterfly", "kamikotized_communication", "The Akumatized Communication ability can be activated by pressing the Activate Power button (default: Y) with an akumatized entity nearby.\nIt will cause a mask to appear on the performer's and target's face and will allow spectation, private chat, remote damage, and akumatization revocation.\n");
        addPageTitle("wiki", "miraculouses", "butterfly", "kamikotized_communication", "Akumatized Communication");

        addPageText("wiki", "miraculouses", "butterfly", "throw", "Throw mode allows right clicking to throw the cane.\nThis will damage any entity or item hit, releasing an Akuma if inside.\n");

        // Cat
        addPageText("wiki", "miraculouses", "cat", "throw", "Throw mode allows right clicking to throw the staff.\nThis will damage any entity or item you hit, releasing an Akuma if inside.\n");

        // Ladybug
        addPageText("wiki", "miraculouses", "ladybug", "attacking", "Because the tool is a Yoyo, it can't melee attack.\nInstead, left clicking will launch a damaging projectile in the direction the user is facing.\nThis will damage any entity or item hit, releasing an Akuma if inside.\n");
        addPageText("wiki", "miraculouses", "ladybug", "miraculous_ladybug", "The Miraculous Ladybug ability can be activated by pressing the Activate Power button (default: Y) with the lucky charm in hand.\nIt will send the lucky charm into the air and summon magic ladybugs that fly around and heal all damage caused by miraculous or akumatization abilities related to the target.\n");
        addPageText("wiki", "miraculouses", "ladybug", "purify", "Purify mode allows capturing and purify Akumas with left click.\nThere is no limit to how many Akumas can be captured,\nand right clicking will release all stored Akumas at once,\nlaunching them upwards.\n");
    }
}
