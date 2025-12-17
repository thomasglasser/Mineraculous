package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.floraandfauna;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.expansions.AkumatizationEntryProvider;

public class KamikosEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikos";

    public KamikosEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("obtaining", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Obtaining");
        pageText("""
                Kamikos can only be obtained via kamikotization by the Butterfly Miraculous holder.
                """);

        page("behavior", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("flora_and_fauna/kamikos/kamiko.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Behavior");
        pageText("""
                Kamikos follow their owner if they're transformed or anyone with an open Butterfly Cane in Kamiko Store mode.
                Otherwise, they fly around aimlessly.
                """);
    }

    @Override
    protected String entryName() {
        return "Kamikos";
    }

    @Override
    protected String entryDescription() {
        return "The magical butterflies created by the [Butterfly Miraculous] (entry://miraculouses/butterfly).";
    }

    @Override
    protected BookIconModel entryIcon() {
        return AkumatizationEntryProvider.ICON;
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
