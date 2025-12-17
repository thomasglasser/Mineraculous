package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.dependencies;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class JeiEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "jei";

    public JeiEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("miraculous", () -> BookImagePageModel.create()
                .withAnchor("miraculous")
                .withImages(WikiBookSubProvider.wikiTexture("dependencies/jei/miraculous.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Miraculous");
        add(context().pageText(), "[Just Enough Items](https://modrinth.com/mod/jei) is able to recognize and show the all data driven miraculous.");
    }

    @Override
    protected String entryName() {
        return "Just Enough Items";
    }

    @Override
    protected String entryDescription() {
        return "Just Enough Items is an item and recipe viewing mod.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.COMPASS);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
