package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.itemstealingandbreaking;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class ItemStealingAndBreakingCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "item_stealing_and_breaking";

    public ItemStealingAndBreakingCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new StealingEntryProvider(this).generate());
        add(new BreakingEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Item Stealing and Breaking";
    }

    @Override
    protected String categoryDescription() {
        return "A unique mod feature that allows you to obtain items from others and break them, releasing anything inside.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.DIAMOND);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
