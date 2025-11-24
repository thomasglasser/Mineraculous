package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.expansions;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;

public class ExpansionsCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "expansions";

    public ExpansionsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new AkumatizationEntryProvider(this).generate());
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
        return AkumatizationEntryProvider.ICON;
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
