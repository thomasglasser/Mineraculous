package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

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
        // Abilities
        // Miraculous
        // Kamikotizations
        add(new AdvancementTriggersEntry(this).generate());
        // Mod Tags
    }

    @Override
    protected String categoryName() {
        return "APIs";
    }

    @Override
    protected String categoryDescription() {
        return "APIs in Mineraculous that allow for datapack and mod customization.";
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
