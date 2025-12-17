package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class ApisCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "apis";

    public ApisCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new AbilitiesEntryProvider(this).generate());
        add(new MiraculousEntryProvider(this).generate());
        add(new KamikotizationsEntryProvider(this).generate());
        add(new AdvancementTriggersEntryProvider(this).generate());
        add(new DataMapsEntryProvider(this).generate());
        add(new TagsEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "APIs";
    }

    @Override
    protected String categoryDescription() {
        return "APIs that allow for addon mod and datapack customization.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.REPEATING_COMMAND_BLOCK);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
