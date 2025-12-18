package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.food;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;

public class CheeseEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "cheese";

    public CheeseEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("ageing", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("food/cheese/ageing"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Ageing");
        pageText("""
                Cheese ages slowly over time in 5 stages:
                - Fresh
                - Aged
                - Ripened
                - Exquisite
                - Time-Honored\\
                Cheese blocks can be waxed with Honeycomb to prevent further ageing.
                They can be scraped with an Axe to start ageing again.
                """);

        AgeingCheeseEdibleFullBlock block = MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.AGED).get();
        ItemStack eaten = block.asItem().getDefaultInstance();
        eaten.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(block.getMissingPiecesProperty(), 1));
        page("eating", () -> BookSpotlightPageModel.create()
                .withItem(eaten)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Eating");
        pageText("""
                Wedges can be eaten in item form on right click.
                Blocks can be eaten by right clicking the block and eating a piece at a time.
                You can add bites back to the block by right clicking it with a matching wedge.
                Blocks will output a comparator signal that changes based on how many bites they have left.
                """);

        page("finding", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousBlocks.CHEESE_POT)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Finding Cheese");
        pageText("""
                Cheese can be found in village Creameries and traded from the Fromager.
                """);

        page("cheese", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousBlocks.CHEESE.get(AgeingCheese.Age.AGED))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cheese");
        pageText("""
                Normal cheese is a nice orange and gets darker with age.
                """);

        page("camembert", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousItems.CAMEMBERT.get(AgeingCheese.Age.AGED))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Camembert");
        pageText("""
                Camembert is a white, creamy, and *very* stinky cheese.
                It also gets darker with age.
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
