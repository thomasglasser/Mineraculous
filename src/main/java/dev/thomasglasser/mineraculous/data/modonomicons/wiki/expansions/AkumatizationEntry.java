package dev.thomasglasser.mineraculous.data.modonomicons.wiki.expansions;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class AkumatizationEntry extends IndexModeEntryProvider {
    private static final String ID = "akumatization";

    public AkumatizationEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("akumatization", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("akumatization/akumatization.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Akumatization");
        pageText("""
                The Akumatization pack replaces all references to "Kamiko" and "Kamikotization" with "Akuma" and "Akumatization".
                """);
    }

    @Override
    protected String entryName() {
        return "Akumatization Pack";
    }

    @Override
    protected String entryDescription() {
        return "Replaces \"Kamiko\" with \"Akuma\"";
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
