package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.floraandfauna;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;

public class FloraAndFaunaCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "flora_and_fauna";

    public FloraAndFaunaCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new FlowersEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Flora and Fauna";
    }

    @Override
    protected String categoryDescription() {
        return "The plants and animals that make up the Mineraculous ecosystem.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(MineraculousBlocks.HIBISCUS_BUSH);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
