package dev.thomasglasser.mineraculous.data.modonomicons.wiki.food;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;

public class CheeseEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "cheese";

    public CheeseEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("cheese", () -> BookImagePageModel.create()
                .withImages(Mineraculous.modLoc("textures/item/aged_wedge_of_cheese.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cheese");
        pageText("""
                Normal cheese is a nice orange and gets darker with age.
                """);

        page("camembert", () -> BookImagePageModel.create()
                .withImages(Mineraculous.modLoc("textures/item/aged_wedge_of_camembert.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Camembert");
        pageText("""
                Camembert is a white, creamy, and *very* stinky cheese.
                """);

        page("finding", () -> BookImagePageModel.create()
                .withImages(Mineraculous.modLoc("textures/item/cheese_pot.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Finding Cheese");
        pageText("""
                Cheese can be found in village Creameries and traded from the Fromager.
                """);
    }

    @Override
    protected String entryName() {
        return "Cheese";
    }

    @Override
    protected String entryDescription() {
        return "A stinky, delectable milk product found in village Creameries";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.CHEESE.get(AgeingCheese.Age.FRESH));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
