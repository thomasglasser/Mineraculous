package dev.thomasglasser.mineraculous.data.modonomicons.wiki.decorations;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class DecorationsCategoryProvider extends IndexModeCategoryProvider {
    private static final String ID = "decorations";

    public DecorationsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new PaintingsEntry(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Decorations";
    }

    @Override
    protected String categoryDescription() {
        return "Decorative blocks to make your world look nice.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.PAINTING);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
