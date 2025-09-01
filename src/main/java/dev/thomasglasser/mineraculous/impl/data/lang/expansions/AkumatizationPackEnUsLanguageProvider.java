package dev.thomasglasser.mineraculous.impl.data.lang.expansions;

import dev.thomasglasser.mineraculous.api.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization.ReceiverKamikotizationChatScreen;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.data.lang.EnUsOverrideLanguageProvider;
import net.minecraft.data.PackOutput;

public class AkumatizationPackEnUsLanguageProvider extends EnUsOverrideLanguageProvider {
    public AkumatizationPackEnUsLanguageProvider(PackOutput output) {
        super(output, MineraculousPacks.AKUMATIZATION.knownPack().id());
    }

    @Override
    protected void addTranslations() {
        addItems();
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

    private void addItems() {
        // Armor
        add(MineraculousArmors.KAMIKOTIZATION, "Akumatization", "Mask", "Chestplate", "Leggings", "Boots");
    }

    private void addEntityTypes() {
        add(MineraculousEntityTypes.KAMIKO.get(), "Akuma");
    }

    private void addAbilities() {
        add(Abilities.KAMIKOTIZATION, "Akumatization");
        add(Abilities.KAMIKO_CONTROL, "Akuma Control");
        add(Abilities.KAMIKOTIZED_COMMUNICATION, "Akumatized Communication");
    }

    private void addGuis() {
        // Kamiko Gui
        add(MineraculousGuis.REVOKE, "Revoke Akumatization");
        add(Kamiko.CANT_KAMIKOTIZE_TRANSFORMED, "Akumatizing transformed players is not currently supported.");
        add(Kamikotization.NO_KAMIKOTIZATIONS, "No Akumatizations found in world, have you installed any addons?");

        // Kamikotization Selection Screen
        add(KamikotizationSelectionScreen.TITLE, "Akumatization");
        add(KamikotizationSelectionScreen.NO_KAMIKOTIZATIONS, "No valid akumatizations found for %s");

        // Receiver Kamikotization Chat Screen
        add(ReceiverKamikotizationChatScreen.ACCEPT, "Accept Akumatization");
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
    }

    private void addAdvancements() {
        addAdvancement("miraculous", "kamikotize_player", "desc", "Provide power to another player with an akumatization");
        addAdvancement("miraculous", "kamikotize_self", "desc", "Use the butterfly miraculous to akumatize yourself");
        addAdvancement("miraculous", "power_kamiko", "desc", "Power up an Akuma");
        addAdvancement("miraculous", "power_kamiko", "title", "Fly Away My Little Akuma");
        addAdvancement("miraculous", "release_purified_kamiko", "desc", "Purify and release an Akuma");
        addAdvancement("miraculous", "transform_kamikotization", "desc", "Accept an akumatization from the Butterfly miraculous holder");
    }

    private void addWiki() {
        addEntryDescription("wiki", "apis", "abilities", "Supernatural abilities that can be used by Miraculous holders or Akumatizations");
        addPageText("wiki", "apis", "advancement_triggers", "kamikotized_player", "This trigger is called when a player akumatizes another player. It has one parameter:\n- \"type\": The type of akumatization that was used.\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "kamikotized_player", "Akumatized Player");
        addPageText("wiki", "apis", "advancement_triggers", "released_purified_kamiko", "This trigger is called when a player releases a purified akuma. It has one parameter:\n- \"count\": The number of purified akumas that were released.\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "released_purified_kamiko", "Released Purified Akuma");
        addPageText("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "This trigger is called when a player is transformed by a akumatization. It has two parameters:\n- \"type\": The type of akumatization that was used.\n- \"self\": Whether the player akumatized themselves.\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "Transformed Akumatization");
        addPageText("wiki", "apis", "advancement_triggers", "used_kamikotization_power", "This trigger is called when a player uses a akumatization ability. It has two parameters:\n- \"type\": The type of akumatization that was used.\n- \"context\": The context in which the power was used. Can be one of the following:\n    - empty\n    - block\n    - entity\n    - living_entity\n    - item\n");
        addPageTitle("wiki", "apis", "advancement_triggers", "used_kamikotization_power", "Used Akumatization Power");
        addPageText("wiki", "apis", "data_maps", "lucky_charms", "Contextual lucky charms are determined for entities, miraculous holders, and akumatizations via data maps.\nThey are located in \"data/<namespace>/entity_type/lucky_charms.json\",\n\"data/<namespace>/mineraculous/miraculous/lucky_charms.json\",\nand \"data/<namespace>/mineraculous/kamikotization/lucky_charms.json\".\nGenerators can be found online [here](https://beta-jsons.thomasglasser.dev/partners/).\n");
        addPageText("wiki", "apis", "kamikotizations", "fields", "Akumatizations have a few fields that determine how they work:\n- active_ability: The ability that is activated when the Activate Power button (default: O) is pressed.\n- default_name: The default name of the akumatization.\n- item_predicate: The predicate that determines if the akumatization can be applied to an item.\n- passive_abilities: The abilities that are active at all times when akumatized.\n");
        addPageText("wiki", "apis", "kamikotizations", "generator", "A generator for akumatizations can be found online [here](https://beta-jsons.thomasglasser.dev/mineraculous/kamikotization/).\n");
        addEntryDescription("wiki", "expansions", "kamikotizations", "An expansion mod adding all Akumatizations from the show into the game.");
        addPageText("wiki", "expansions", "kamikotizations", "description", "For customization purposes, the base mod adds no Akumatizations by default.\nInstead, the Kamikotizations expansion mod is available to add all Akumatizations from the show into the game.\nUpdates release alongside the base mod,\nadding all the Akumatizations from that season.\n");
        addEntryName("wiki", "flora_and_fauna", "kamikos", "Akumas");
        addPageText("wiki", "flora_and_fauna", "kamikos", "obtaining", "Akumas can only be obtained via spawn egg in the creative menu.\n");
        addPageText("wiki", "flora_and_fauna", "kamikos", "powered", "Once powered, akumas follow their owner if they're transformed.\nIf not, they also fly around aimlessly.\n");
        addPageText("wiki", "flora_and_fauna", "kamikos", "unpowered", "Unpowered akumas are normal butterflies.\nThey just fly around aimlessly.\nIf an open butterfly cane or powered butterfly miraculous holder are nearby, they will fly around it.\n");
        addCategoryName("wiki", "kamikotizations", "Akumatizations");
        addCategoryDescription("wiki", "kamikotizations", "Powers given by the [Butterfly Miraculous](entry://miraculous/butterfly) to normal players. Addons can add entries to this category describing their akumatizations (see [this page](https://klikli-dev.github.io/modonomicon/docs/basics/structure/entries) for more info).");
        addEntryDescription("wiki", "kamikotizations", "customization", "Per-player akumatization customization");
        addPageText("wiki", "kamikotizations", "customization", "overriding", "NOTE: The following features are only available if the server manually enables it.\\\n\\\nThe look of your akumatizations can be further customized by putting files in the 'kamikotizations' subfolder of the 'miraculouslooks' folder in the client's minecraft directory.\\\nA guide for customizing can be found [here](https://beta-jsons.thomasglasser.dev/guides/customization/).\n");
        addEntryDescription("wiki", "kamikotizations", "kamikotizations_general", "Features of all akumatizations");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "receiving", "The only way to receive an akumatization is from a powered [Akuma](entry://flora_and_fauna/kamikos).\nThe owner of the akuma will choose an akumatization with powers for you based on the items in your inventory.\nIt will then send the akuma to you and it will enter that item.\nYou have the option to accept the powers, or you can reject the akumatization if the server allows it.\n");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "revoking", "Akumatizations are revoked when the akumatized item is destroyed or when the [Butterfly Miraculous](entry://miraculous/butterfly) holder chooses to revoke the akumatization.\nThis will release the akuma from the item and remove the akumatization and powers.\n");
        addPageText("wiki", "kamikotizations", "kamikotizations_general", "using_tool", "When you are akumatized, your akumatized item will either be turned into a tool or you will be given an ability to use on key press (default: O).\nThis tool and ability will be different depending on the akumatization, so refer to the [Akumatizations](category://kamikotizations) category for more info.\n");
        addPageText("wiki", "miraculous", "butterfly", "abilities", "The Butterfly Miraculous has 3 abilities:\n- Akumatization\n- Akuma Control\n- Akumatized Communication\n");
        addPageText("wiki", "miraculous", "butterfly", "cane_abilities", "The Butterfly Miraculous has 3 abilities:\n- Blade\n- Block\n- Akuma Store\n- Throw\n");
        addPageText("wiki", "miraculous", "butterfly", "kamiko_control", "The Akuma Control ability can be activated by pressing the Activate Power button (default: O) with a powered [Akuma](entry://flora_and_fauna/kamikos) nearby.\nIt will cause a mask to appear on your face and will allow you to see through the eyes of the [Akuma](entry://flora_and_fauna/kamikos).\nYou can then press the number keys to select a target that the [Akuma](entry://flora_and_fauna/kamikos) will fly to.\nOnce it reaches the target, it will open the Akumatization Selection Screen and allow you to akumatize the target.\n");
        addPageTitle("wiki", "miraculous", "butterfly", "kamiko_control", "Akuma Control");
        addPageText("wiki", "miraculous", "butterfly", "kamiko_store", "Akuma Store mode allows you to right click to store a powered [Akuma](entry://flora_and_fauna/kamikos).\nYou can then right click again to release the [Akuma](entry://flora_and_fauna/kamikos).\n");
        addPageTitle("wiki", "miraculous", "butterfly", "kamiko_store", "Akuma Store");
        addPageText("wiki", "miraculous", "butterfly", "kamikotization", "The Akumatization ability can be activated by pressing the Activate Power button (default: O) with no powered [Akumas](entry://flora_and_fauna/kamikos) nearby.\nIt will cause particles to come from your hand.\nYou can then interact with an unpowered [Akuma](entry://flora_and_fauna/kamikos) to power and tame it.\n");
        addPageTitle("wiki", "miraculous", "butterfly", "kamikotization", "Akumatization");
        addPageText("wiki", "miraculous", "butterfly", "kamikotized_communication", "The Akumatized Communication ability can be activated by pressing the Activate Power button (default: O) with a akumatized player nearby.\nIt will cause a mask to appear on your face and the player's face and will allow you to see through their eyes.\nYou can then speak to the player or revoke their akumatization.\n");
        addPageTitle("wiki", "miraculous", "butterfly", "kamikotized_communication", "Akumatized Communication");
        addPageText("wiki", "miraculous", "customization", "kamikotization", "Akumatization looks can be customized in the 'kamikotizations' subfolder.\nYou can provide a custom texture, model, glowmask, and transforms with an id that is used in the command.\nThe only required file is the texture.\nIf only the texture is provided, it will use the default model.\n");
        addPageTitle("wiki", "miraculous", "customization", "kamikotization", "Akumatization");
        addPageText("wiki", "miraculous", "ladybug", "purify", "Purify mode allows you to catch and purify powered [Akumas](entry://flora_and_fauna/kamikos) with left click.\nYou can capture as many as you want and right click to release them all at once.\n");
    }
}
