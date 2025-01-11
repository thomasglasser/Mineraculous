package dev.thomasglasser.mineraculous.data.modonomicons.wiki.dependencies;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class DependenciesCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "dependencies";

    public DependenciesCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new JeiEntry(this).generate());
        add(new ModonomiconEntry(this).generate());
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
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.LADYBUG));
    }
}
