package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.decorations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;

public class SherdsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "sherds";

    public SherdsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("ladybug", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousItems.LADYBUG_POTTERY_SHERD)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Ladybug");
        add(context().pageText(), """
                The Ladybug sherd can be found in Desert Pyramid archeology.
                """);
    }

    @Override
    protected String entryName() {
        return "Pottery Sherds";
    }

    @Override
    protected String entryDescription() {
        return "Pottery sherd items that can be used to craft Decorated Pots.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.LADYBUG_POTTERY_SHERD);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
