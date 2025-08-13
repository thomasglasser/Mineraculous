package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.food;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;

public class FoodCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "food";

    public FoodCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new CheeseEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Food";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(MineraculousItems.CHEESE.get(AgeingCheese.Age.FRESH));
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
