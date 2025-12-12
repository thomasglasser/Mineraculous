package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.cosmetics;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class DevEntry extends IndexModeEntryProvider {
    private static final String ID = "dev";

    public DevEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("legacy", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("cosmetics/dev/legacy"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Legacy Development Team");
        add(context().pageText(), "Members of the development team that joined the project before the release of 1.0.0 get access to an exclusive beard cosmetic.");

        page("configuration", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Configuration");
        add(context().pageText(), """
                Dev Team Cosmetics have four client configuration options:
                - "display_self_dev_team_cosmetic" - Whether or not to display your own Dev Team cosmetic.
                - "display_self_legacy_dev_team_cosmetic" - Whether or not to display your own Legacy Dev Team cosmetic.
                - "display_others_dev_team_cosmetic" - Whether or not to display other players' Dev Team cosmetics.
                - "display_others_legacy_dev_team_cosmetic" - Whether or not to display other players' Legacy Dev Team cosmetics.
                """);
    }

    @Override
    protected String entryName() {
        return "Development Team";
    }

    @Override
    protected String entryDescription() {
        return "Exclusive cosmetics for members of the Development Team.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.STRUCTURE_BLOCK);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
