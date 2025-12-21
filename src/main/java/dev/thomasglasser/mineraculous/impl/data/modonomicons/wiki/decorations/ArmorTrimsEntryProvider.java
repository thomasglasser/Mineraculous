package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.decorations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class ArmorTrimsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "armor_trims";

    public ArmorTrimsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("ladybug", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("decorations/armor_trims/ladybug"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Ladybug");
        add(context().pageText(), """
                The Ladybug armor trim can be found in Desert Pyramid archeology.
                """);

        page("cat", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("decorations/armor_trims/cat"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Cat");
        add(context().pageText(), """
                The Cat armor trim can be found in Desert Pyramid archeology.
                """);

        page("butterfly", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("decorations/armor_trims/butterfly"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Butterfly");
        add(context().pageText(), """
                The Butterfly armor trim can be found in Desert Pyramid archeology.
                """);
    }

    @Override
    protected String entryName() {
        return "Armor Trims";
    }

    @Override
    protected String entryDescription() {
        return "Armor trim items that can be applied to armor for cosmetic purposes.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
