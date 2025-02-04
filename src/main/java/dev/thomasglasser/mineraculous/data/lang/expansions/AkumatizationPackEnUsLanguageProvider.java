package dev.thomasglasser.mineraculous.data.lang.expansions;

import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationChatScreen;
import dev.thomasglasser.mineraculous.client.gui.screens.KamikotizationSelectionScreen;
import dev.thomasglasser.mineraculous.packs.MineraculousPacks;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.data.lang.ExtendedEnUsLanguageProvider;
import net.minecraft.data.PackOutput;

public class AkumatizationPackEnUsLanguageProvider extends ExtendedEnUsLanguageProvider {
    public AkumatizationPackEnUsLanguageProvider(PackOutput output) {
        super(output, MineraculousPacks.AKUMATIZATION.knownPack().id());
    }

    @Override
    protected void addTranslations() {
        add(MineraculousItems.KAMIKO_SPAWN_EGG.get(), "Akuma Spawn Egg");

        add(MineraculousArmors.KAMIKOTIZATION.HEAD.get(), "Akumatization Mask");
        add(MineraculousArmors.KAMIKOTIZATION.CHEST.get(), "Akumatization Chestplate");
        add(MineraculousArmors.KAMIKOTIZATION.LEGS.get(), "Akumatization Leggings");
        add(MineraculousArmors.KAMIKOTIZATION.FEET.get(), "Akumatization Boots");

        add(MineraculousEntityTypes.KAMIKO.get(), "Akuma");

        add(MineraculousAbilities.KAMIKOTIZATION, "Akumatization");
        add(MineraculousAbilities.KAMIKO_CONTROL, "Akuma Control");
        add(MineraculousAbilities.KAMIKOTIZED_COMMUNICATION, "Akumatized Communication");

        add(Kamiko.CANT_KAMIKOTIZE_TRANSFORMED, "Akumatizing transformed players is not currently supported.");

        add(KamikotizationSelectionScreen.TITLE, "Akumatization");
        add(KamikotizationSelectionScreen.NO_KAMIKOTIZATIONS, "No valid akumatizations found for %s");

        add(KamikotizationChatScreen.ACCEPT, "Accept Akumatization");

        add(MineraculousClientEvents.REVOKE, "Revoke Akumatization");

        add(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE.get(), "Akuma powers up");
        add(MineraculousSoundEvents.KAMIKOTIZATION_USE.get(), "Akumatization begins");

        add(MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM.get(), "Akumatized player transforms");
        add(MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM.get(), "Akumatized player detransforms");

        addConfig(MineraculousServerConfig.get().enableKamikotizationRejection, "Enable Akumatization Rejection", "Enable rejection of akumatization by the victim");

        // Advancements
        addAdvancement("miraculous", "kamikotize_player", "desc", "Provide power to another player with an akumatization");
        addAdvancement("miraculous", "kamikotize_self", "desc", "Use the butterfly miraculous to akumatize yourself");
        addAdvancement("miraculous", "power_kamiko", "desc", "Power up an Akuma");
        addAdvancement("miraculous", "power_kamiko", "title", "Fly Away My Little Akuma");
        addAdvancement("miraculous", "release_purified_kamiko", "desc", "Purify and release an Akuma");
        addAdvancement("miraculous", "transform_kamikotization", "desc", "Accept an akumatization from the Butterfly miraculous holder");

        // Modonomicon
        addEntryDescription("wiki", "apis", "abilities", "Supernatural abilities that can be used by Miraculous holders or Akumatizations");
        addPage("wiki", "apis", "advancement_triggers", "kamikotized_player", "text", "This trigger is called when a player akumatizes another player. It has one parameter:\n- \"type\": The type of akumatization that was used.\n");
        addPage("wiki", "apis", "advancement_triggers", "kamikotized_player", "title", "Akumatized Player");
        addPage("wiki", "apis", "advancement_triggers", "released_purified_kamiko", "text", "This trigger is called when a player releases a purified akuma. It has one parameter:\n- \"count\": The number of purified akumas that were released.\n");
        addPage("wiki", "apis", "advancement_triggers", "released_purified_kamiko", "title", "Released Purified Akuma");
        addPage("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "text", "This trigger is called when a player is transformed by a akumatization. It has two parameters:\n- \"type\": The type of akumatization that was used.\n- \"self\": Whether the player akumatized themselves.\n");
        addPage("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "title", "Transformed Akumatization");
        addPage("wiki", "apis", "advancement_triggers", "used_kamikotization_power", "text", "This trigger is called when a player uses a akumatization ability. It has two parameters:\n- \"type\": The type of akumatization that was used.\n- \"context\": The context in which the power was used. Can be one of the following:\n    - empty\n    - block\n    - entity\n    - living_entity\n    - item\n");
        addPage("wiki", "apis", "advancement_triggers", "used_kamikotization_power", "title", "Used Akumatization Power");
        addPage("wiki", "apis", "data_maps", "lucky_charms", "text", "Contextual lucky charms are determined for entities, miraculous holders, and akumatizations via data maps.\nThey are located in \"data/<namespace>/entity_type/lucky_charms.json\",\n\"data/<namespace>/mineraculous/miraculous/lucky_charms.json\",\nand \"data/<namespace>/mineraculous/kamikotization/lucky_charms.json\".\nGenerators can be found online [here](https://jsons.thomasglasser.dev/partners/).\n");
        addPage("wiki", "apis", "kamikotizations", "fields", "text", "Akumatizations have a few fields that determine how they work:\n- active_ability: The ability that is activated when the Activate Power button (default: O) is pressed.\n- default_name: The default name of the akumatization.\n- item_predicate: The predicate that determines if the akumatization can be applied to an item.\n- passive_abilities: The abilities that are active at all times when akumatized.\n");
        addPage("wiki", "apis", "kamikotizations", "generator", "text", "A generator for akumatizations can be found online [here](https://jsons.thomasglasser.dev/mineraculous/kamikotization/).\n");
        addEntryName("wiki", "flora_and_fauna", "kamikos", "Akumas");
        addPage("wiki", "flora_and_fauna", "kamikos", "obtaining", "text", "Akumas can only be obtained via spawn egg in the creative menu.\n");
        addPage("wiki", "flora_and_fauna", "kamikos", "powered", "text", "Once powered, akumas follow their owner if they're transformed.\nIf not, they also fly around aimlessly.\n");
        addPage("wiki", "flora_and_fauna", "kamikos", "unpowered", "text", "Unpowered akumas are normal butterflies.\nThey just fly around aimlessly.\nIf an open butterfly cane or powered butterfly miraculous holder are nearby, they will fly around it.\n");
        addPage("wiki", "miraculous", "butterfly", "abilities", "text", "The Butterfly Miraculous has 3 abilities:\n- Akumatization\n- Akuma Control\n- Akumatized Communication\n");
        addPage("wiki", "miraculous", "butterfly", "cane_abilities", "text", "The Butterfly Miraculous has 3 abilities:\n- Blade\n- Block\n- Akuma Store\n- Throw\n");
        addPage("wiki", "miraculous", "butterfly", "kamiko_control", "text", "The Akuma Control ability can be activated by pressing the Activate Power button (default: O) with a powered [Akuma](entry://flora_and_fauna/kamikos) nearby.\nIt will cause a mask to appear on your face and will allow you to see through the eyes of the [Akuma](entry://flora_and_fauna/kamikos).\nYou can then press the number keys to select a target that the [Akuma](entry://flora_and_fauna/kamikos) will fly to.\nOnce it reaches the target, it will open the Akumatization Selection Screen and allow you to akumatize the target.\n");
        addPage("wiki", "miraculous", "butterfly", "kamiko_control", "title", "Akuma Control");
        addPage("wiki", "miraculous", "butterfly", "kamiko_store", "text", "Akuma Store mode allows you to right click to store a powered [Akuma](entry://flora_and_fauna/kamikos).\nYou can then right click again to release the [Akuma](entry://flora_and_fauna/kamikos).\n");
        addPage("wiki", "miraculous", "butterfly", "kamiko_store", "title", "Akuma Store");
        addPage("wiki", "miraculous", "butterfly", "kamikotization", "text", "The Akumatization ability can be activated by pressing the Activate Power button (default: O) with no powered [Akumas](entry://flora_and_fauna/kamikos) nearby.\nIt will cause particles to come from your hand.\nYou can then interact with an unpowered [Akuma](entry://flora_and_fauna/kamikos) to power and tame it.\n");
        addPage("wiki", "miraculous", "butterfly", "kamikotization", "title", "Akumatization");
        addPage("wiki", "miraculous", "butterfly", "kamikotized_communication", "text", "The Akumatized Communication ability can be activated by pressing the Activate Power button (default: O) with a akumatized player nearby.\nIt will cause a mask to appear on your face and the player's face and will allow you to see through their eyes.\nYou can then speak to the player or revoke their akumatization.\n");
        addPage("wiki", "miraculous", "butterfly", "kamikotized_communication", "title", "Akumatized Communication");
        addPage("wiki", "miraculous", "customization", "kamikotization", "text", "Akumatization looks can be customized in the 'kamikotizations' subfolder.\nYou can provide a custom texture, model, glowmask, and transforms with an id that is used in the command.\nThe only required file is the texture.\nIf only the texture is provided, it will use the default model.\n");
        addPage("wiki", "miraculous", "customization", "kamikotization", "title", "Akumatization");
        addPage("wiki", "miraculous", "ladybug", "purify", "text", "Purify mode allows you to catch and purify powered [Akumas](entry://flora_and_fauna/kamikos) with left click.\nYou can capture as many as you want and right click to release them all at once.\n");
    }

    private void addAdvancement(String category, String id, String suffix, String name) {
        add("advancement.mineraculous." + category + "." + id + "." + suffix, name);
    }

    private void addPage(String book, String category, String entry, String page, String suffix, String name) {
        add("book.mineraculous." + book + "." + category + "." + entry + "." + page + "." + suffix, name);
    }

    private void addCategoryDescription(String book, String category, String name) {
        add("book.mineraculous." + book + "." + category + ".description", name);
    }

    private void addEntryDescription(String book, String category, String entry, String name) {
        add("book.mineraculous." + book + "." + category + "." + entry + ".description", name);
    }

    private void addEntryName(String book, String category, String entry, String name) {
        add("book.mineraculous." + book + "." + category + "." + entry + ".name", name);
    }

    @Override
    public String getName() {
        return "Akumatization Pack " + super.getName();
    }
}
