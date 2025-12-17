package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.floraandfauna;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class FlowersEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "flowers";

    public FlowersEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("hibiscus", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("flora_and_fauna/flowers/hibiscus.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Hibiscus");
        pageText("""
                Hibiscus is a flower found by sniffers.
                Once obtained, it can be planted to make a hibiscus bush.
                This allows for renewable hibiscus once you have one.
                """);
    }

    @Override
    protected String entryName() {
        return "Flowers";
    }

    @Override
    protected String entryDescription() {
        return "The decorative florals of the mod";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousBlocks.HIBISCUS_BUSH);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
