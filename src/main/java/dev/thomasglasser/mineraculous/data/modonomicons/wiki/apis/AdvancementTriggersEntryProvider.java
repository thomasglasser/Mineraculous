package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class AdvancementTriggersEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "advancement_triggers";

    public AdvancementTriggersEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for advancements can be found online [here](https://snapshot-jsons.thomasglasser.dev/advancement/).
                """);

        page("transformed_miraculous", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Transformed Miraculous");
        add(context().pageText(), """
                This trigger is called when a player transforms with a miraculous. It has one parameter:
                - "type": The type of miraculous that was transformed.
                """);

        page("used_miraculous_power", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Used Miraculous Power");
        add(context().pageText(), """
                This trigger is called when a player uses a miraculous power. It has two parameters:
                - "type": The type of miraculous that was used.
                - "context": The context in which the power was used. Can be one of the following:
                    - empty
                    - block
                    - entity
                    - living_entity
                    - item
                """);

        page("kamikotized_player", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Kamikotized Player");
        add(context().pageText(), """
                This trigger is called when a player kamikotizes another player. It has one parameter:
                - "type": The type of kamikotization that was used.
                """);

        page("transformed_kamikotization", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Transformed Kamikotization");
        add(context().pageText(), """
                This trigger is called when a player is transformed by a kamikotization. It has two parameters:
                - "type": The type of kamikotization that was used.
                - "self": Whether the player kamikotized themselves.
                """);

        page("used_kamikotization_power", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Used Kamikotization Power");
        add(context().pageText(), """
                This trigger is called when a player uses a kamikotization ability. It has two parameters:
                - "type": The type of kamikotization that was used.
                - "context": The context in which the power was used. Can be one of the following:
                    - empty
                    - block
                    - entity
                    - living_entity
                    - item
                """);

        page("released_purified_kamiko", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Released Purified Kamiko");
        add(context().pageText(), """
                This trigger is called when a player releases a purified kamiko. It has one parameter:
                - "count": The number of purified kamikos that were released.
                """);
    }

    @Override
    protected String entryName() {
        return "Advancement Triggers";
    }

    @Override
    protected String entryDescription() {
        return "Mineraculous adds a few advancement triggers for mods and datapacks to detect and use.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.BUTTERFLY));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
