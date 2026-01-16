package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import net.minecraft.world.item.Items;

public class LooksEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "looks";

    public LooksEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for looks can be found [here](https://beta-jsons.thomasglasser.dev/mineraculous/look/).
                """);

        page("guide", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Guide");
        add(context().pageText(), """
                A guide for creating looks can be found [here](https://beta-jsons.thomasglasser.dev/guides/looks/).
                """);
    }

    @Override
    protected String entryName() {
        return "Looks";
    }

    @Override
    protected String entryDescription() {
        return "User-provided visual customization.";
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
