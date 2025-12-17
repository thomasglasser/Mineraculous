package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;

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
                A generator for advancements can be found online [here](https://beta-jsons.thomasglasser.dev/advancement/).
                """);

        page("transformed_miraculous", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Transformed Miraculous");
        add(context().pageText(), """
                This trigger is called when a player transforms with a miraculous.
                It has two parameters:
                - "player": The player that was transformed.
                - "miraculous": The miraculous that was used to transform.
                """);

        page("performed_miraculous_active_ability", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Performed Miraculous Active Ability");
        add(context().pageText(), """
                This trigger is called when a player performs an active ability provided by their miraculous.
                It has three parameters:
                - "player": The player that performed the ability.
                - "miraculous": The miraculous that provided the ability.
                - "context": The context in which the power was used.
                Can be any value for addon support,
                but the ones included in the mod by default are:
                    - block
                    - entity
                    - living_entity
                """);

        page("kamikotized_entity", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Kamikotized Entity");
        add(context().pageText(), """
                This trigger is called when a player kamikotizes an entity.
                It has four parameters:
                - "player": The player that kamikotized the entity.
                - "target": The entity that was kamikotized.
                - "kamikotization": The kamikotization that was given to the entity.
                - "self": Whether the player kamikotized themself.
                """);

        page("released_purified_entities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Released Purified Entities");
        add(context().pageText(), """
                This trigger is called when a player releases purified entities.
                It has three parameters:
                - "player": The player that released the entities.
                - "released": The entities that were released.
                - "count": The number of purified kamikos that were released.
                """);

        page("transformed_kamikotization", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Transformed Kamikotization");
        add(context().pageText(), """
                This trigger is called when a player is transformed by a kamikotization.
                It has three parameters:
                - "player": The player that was transformed.
                - "kamikotization": The kamikotization that was used to transform.
                - "self": Whether the player kamikotized themself.
                """);

        page("performed_kamikotization_active_ability", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Performed Kamikotization Active Ability");
        add(context().pageText(), """
                This trigger is called when a player performs an active ability provided by their kamikotization.
                It has three parameters:
                - "player": The player that performed the ability.
                - "kamikotization": The kamikotization that provided the ability.
                - "context": The context in which the power was used.
                Can be any value for addon support,
                but the ones included in the mod by default are:
                    - block
                    - entity
                    - living_entity
                """);
    }

    @Override
    protected String entryName() {
        return "Advancement Triggers";
    }

    @Override
    protected String entryDescription() {
        return "Advancement triggers for addon mods and datapacks to detect and use.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(registries().holderOrThrow(Miraculouses.BUTTERFLY)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
