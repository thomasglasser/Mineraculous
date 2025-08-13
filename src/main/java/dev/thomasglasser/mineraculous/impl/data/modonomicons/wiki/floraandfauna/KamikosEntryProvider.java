package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.floraandfauna;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

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
                Kamikos can only be obtained via spawn egg in the creative menu.
                """);

        page("unpowered", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("flora_and_fauna/kamikos/unpowered.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Unpowered");
        pageText("""
                Unpowered kamikos are normal butterflies.
                They just fly around aimlessly.
                If an open butterfly cane or powered butterfly miraculous holder are nearby, they will fly around it.
                """);

        page("powered", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("flora_and_fauna/kamikos/powered.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Powered");
        pageText("""
                Once powered, kamikos follow their owner if they're transformed.
                If not, they also fly around aimlessly.
                """);
    }

    @Override
    protected String entryName() {
        return "Kamikos";
    }

    @Override
    protected String entryDescription() {
        return "The magical butterflies that can be powered up by the [Butterfly Miraculous] (entry://miraculous/butterfly).";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(WikiBookSubProvider.wikiTexture("flora_and_fauna/kamikos/powered.png"));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
