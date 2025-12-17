package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.cosmetics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class BetaEntry extends IndexModeEntryProvider {
    private static final String ID = "beta";

    public BetaEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("season_1", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("cosmetics/beta/season_1.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Season 1");
        add(context().pageText(), "Testers for 1.0.0 (Season 1) get access to a Derby Hat cosmetic.");

        page("configuration", () -> BookTextPageModel.create()
                .withAnchor("configuration")
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Configuration");
        add(context().pageText(), """
                Beta Cosmetics have two client configuration options:
                - "display_beta_tester_cosmetic" - Whether or not to display the Beta Tester cosmetic.
                - "beta_tester_cosmetic_choice" - The cosmetic to display for the Beta Tester.
                """);
    }

    @Override
    protected String entryName() {
        return "Beta Testers";
    }

    @Override
    protected String entryDescription() {
        return "Exclusive cosmetics for members of the Beta Program.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.SILVERFISH_SPAWN_EGG);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
