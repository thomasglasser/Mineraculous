package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.dependencies;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class JadeEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "jade";

    public JadeEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("oven", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("dependencies/jade/oven"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Oven");
        add(context().pageText(), """
                The Jade overlay allows you to view the contents and cook progress of the oven.
                """);
    }

    @Override
    protected String entryName() {
        return "Jade";
    }

    @Override
    protected String entryDescription() {
        return "Jade is a block and entity information viewing mod.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.SPYGLASS);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
