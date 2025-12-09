package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.dependencies;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class DependenciesCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "dependencies";

    public DependenciesCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new JeiEntryProvider(this).generate());
        add(new JadeEntryProvider(this).generate());
        add(new ModonomiconEntryProvider(this).generate());
        add(new VoiceChatEntryProvider(this).generate());
    }

    @Override
    public String categoryId() {
        return ID;
    }

    @Override
    protected String categoryName() {
        return "Optional Dependencies";
    }

    @Override
    protected String categoryDescription() {
        return "Mods that are not required but enhance the Mineraculous experience.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.ENCHANTED_BOOK);
    }
}
