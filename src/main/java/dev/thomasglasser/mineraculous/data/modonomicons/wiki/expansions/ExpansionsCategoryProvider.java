package dev.thomasglasser.mineraculous.data.modonomicons.wiki.expansions;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class ExpansionsCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "expansions";

    public ExpansionsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        // Kamikotizations
    }

    @Override
    protected String categoryName() {
        return "Expansions";
    }

    @Override
    protected String categoryDescription() {
        return "Official expansion mods and packs that enhance the Mineraculous experience.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.NETHER_STAR);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
