package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;

public class TagsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "tags";

    public TagsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for vanilla tags can be found online [here](https://snapshot-jsons.thomasglasser.dev/generators/) and mod tags can be found online [here](https://snapshot-jsons.thomasglasser.dev/partners/).
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Block");
        add(context().pageText(), """
                There is one block tag in the mod for cataclysm immune blocks.
                """);

        page("item", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Item");
        add(context().pageText(), """
                There are tags for all of the kwami foods and treats.
                There are also lucky charm tags for general situations, guardians, and warden distractors.
                There is a tag for cataclysm immune items and tough items that take longer to break.
                There are tags for cheeses as well.
                """);

        page("damage_type", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Damage Type");
        add(context().pageText(), """
                There is a damage type tag for those resisted by miraculous.
                """);
    }

    @Override
    protected String entryName() {
        return "Tags";
    }

    @Override
    protected String entryDescription() {
        return "Lists used for mod functionality";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
