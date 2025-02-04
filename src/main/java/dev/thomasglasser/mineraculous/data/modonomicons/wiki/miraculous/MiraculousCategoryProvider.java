package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class MiraculousCategoryProvider extends IndexModeCategoryProvider {
    private static final String ID = "miraculous";

    public MiraculousCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new GeneralEntry(this).generate());
        add(new LadybugEntry(this).generate());
        add(new CatEntry(this).generate());
        add(new ButterflyEntry(this).generate());
        add(new CustomizationEntry(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Miraculous";
    }

    @Override
    protected String categoryDescription() {
        return "Magical jewels that provide amazing abilities";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.LADYBUG));
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
