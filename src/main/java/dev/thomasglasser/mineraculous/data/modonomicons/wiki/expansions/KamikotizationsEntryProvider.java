package dev.thomasglasser.mineraculous.data.modonomicons.wiki.expansions;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class KamikotizationsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikotizations";

    public KamikotizationsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("description", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("kamikotizations/description.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Description");
        pageText("""
                For customization purposes, the base mod adds no Kamikotizations by default.
                Instead, the Kamikotizations expansion mod is available to add all Kamikotizations from the show into the game.
                Updates release alongside the base mod,
                adding all the Kamikotizations from that season.
                """);

        page("links", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("kamikotizations/links.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Links");
        pageText("""
                The Kamikotizations expansion mod can be found on
                [Modrinth](https://modrinth.com/mod/mineraculouskamikotizations),
                [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mineraculouskamikotizations),
                and [GitHub](https://github.com/thomasglasser/Mineraculous-Expansion-Kamikotizations).
                """);
    }

    @Override
    protected String entryName() {
        return "Kamikotizations Expansion";
    }

    @Override
    protected String entryDescription() {
        return "An expansion mod adding all Kamikotizations from the show into the game.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.HEAD);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
