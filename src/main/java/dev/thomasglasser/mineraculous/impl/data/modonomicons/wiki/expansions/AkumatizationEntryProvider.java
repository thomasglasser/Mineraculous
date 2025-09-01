package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.expansions;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class AkumatizationEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "akumatization";

    public AkumatizationEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("description", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("akumatization/description.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Description");
        pageText("""
                The Akumatization Pack replaces all references to "Kamiko" and "Kamikotization" with "Akuma" and "Akumatization".
                """);
    }

    @Override
    protected String entryName() {
        return "Akumatization Pack";
    }

    @Override
    protected String entryDescription() {
        return "Replaces \"Kamiko\" with \"Akuma\" in all cases";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(WikiBookSubProvider.wikiTexture("akumatization/icon.png"));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
