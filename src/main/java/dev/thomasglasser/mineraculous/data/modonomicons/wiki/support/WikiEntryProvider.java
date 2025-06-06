package dev.thomasglasser.mineraculous.data.modonomicons.wiki.support;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import net.minecraft.world.item.Items;

public class WikiEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "wiki";

    public WikiEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("in_game", () -> BookTextPageModel.create()
                .withAnchor("in_game")
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "In-Game Wiki");
        add(context().pageText(), """
                The mod has an in-game wiki when [Modonomicon](entry://dependencies/modonomicon) is installed.
                This wiki contains information about the mod and its features.
                """);

        page("online", () -> BookTextPageModel.create()
                .withAnchor("online")
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Online Wiki");
        add(context().pageText(), """
                The wiki is also hosted online [here](https://mineraculous-snapshot.wiki.thomasglasser.dev).
                """);
    }

    @Override
    protected String entryName() {
        return "Wiki";
    }

    @Override
    protected String entryDescription() {
        return "Locations to find information about the mod.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.ENCHANTED_BOOK);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
