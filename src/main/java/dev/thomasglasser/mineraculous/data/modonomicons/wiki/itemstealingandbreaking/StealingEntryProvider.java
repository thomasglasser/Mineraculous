package dev.thomasglasser.mineraculous.data.modonomicons.wiki.itemstealingandbreaking;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class StealingEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "stealing";

    public StealingEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("stealing", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("item_stealing_and_breaking/stealing.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Stealing");
        pageText("""
                You can steal items from players by holding the Take/Break Item key (default: I).
                """);

        page("configuration", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Configuration");
        pageText("""
                In the server config, you can configure the stealing duration and who can be stolen from.
                - 'stealing_duration' is the duration in seconds that the key must be held to steal an item.
                - 'enable_universal_stealing' enables item stealing from all players all the time.
                - 'enable_sleep_stealing' enables item stealing from players while they sleep.
                - 'wake_up_chance' is the percent chance that a player will wake up while being stolen from.
                """);
    }

    @Override
    protected String entryName() {
        return "Stealing";
    }

    @Override
    protected String entryDescription() {
        return "Forcefully taking items from players";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.DIAMOND_BLOCK);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
