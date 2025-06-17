package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;

public class KamikotizationsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikotizations";

    public KamikotizationsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for kamikotizations can be found online [here](https://beta-jsons.thomasglasser.dev/mineraculous/kamikotization/).
                """);

        page("guide", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Guide");
        add(context().pageText(), """
                A guide for creating miraculous can be found [here](https://beta-jsons.thomasglasser.dev/guides/miraculous/).
                """);
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
