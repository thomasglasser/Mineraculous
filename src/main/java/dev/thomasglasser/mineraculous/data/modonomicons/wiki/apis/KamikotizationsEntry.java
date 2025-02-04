package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class KamikotizationsEntry extends IndexModeEntryProvider {
    private static final String ID = "kamikotizations";

    public KamikotizationsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for kamikotizations can be found online [here](https://jsons.thomasglasser.dev/mineraculous/kamikotization/).
                """);

        page("fields", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Fields");
        add(context().pageText(), """
                Kamikotizations have a few fields that determine how they work:
                - active_ability: The ability that is activated when the Activate Power button (default: O) is pressed.
                - default_name: The default name of the kamikotization.
                - item_predicate: The predicate that determines if the kamikotization can be applied to an item.
                - passive_abilities: The abilities that are active at all times when kamikotized.
                """);

//        page("example", () -> BookTextPageModel.create()
//                .withTitle(context().pageTitle())
//                .withText(context().pageText()));
    }

    @Override
    protected String entryName() {
        return "Kamikotizations";
    }

    @Override
    protected String entryDescription() {
        return "Transformations that the Butterfly Miraculous can give to players";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.HEAD);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
