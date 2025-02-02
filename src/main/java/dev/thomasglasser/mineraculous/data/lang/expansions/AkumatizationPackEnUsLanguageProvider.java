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
        addModonomiconPage("wiki", "apis", "advancement_triggers", "kamikotized_player", "text", "This trigger is called when a player akumatizes another player. It has one parameter:\n- \"type\": The type of akumatization that was used.\n");
        addModonomiconPage("wiki", "apis", "advancement_triggers", "kamikotized_player", "title", "Akumatized Player");
        addModonomiconPage("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "text", "This trigger is called when a player is transformed by an akumatization. It has two parameters:\n- \"type\": The type of akumatization that was used.\n- \"self\": Whether the player akumatized themselves.\n");
        addModonomiconPage("wiki", "apis", "advancement_triggers", "transformed_kamikotization", "title", "Transformed Akumatization");
        addModonomiconDescription("wiki", "cosmetics", "Per-player customization for Miraculous and Akumatizations.");
    }

    private void addAdvancement(String category, String id, String suffix, String name) {
        add("advancement.mineraculous." + category + "." + id + "." + suffix, name);
    }

    private void addModonomiconPage(String book, String category, String entry, String page, String suffix, String name) {
        add("book.mineraculous." + book + "." + category + "." + entry + "." + page + "." + suffix, name);
    }

    private void addModonomiconDescription(String book, String category, String name) {
        add("book.mineraculous." + book + "." + category + ".description", name);
    }

    @Override
    public String getName() {
        return "Akumatization Pack " + super.getName();
    }
}
