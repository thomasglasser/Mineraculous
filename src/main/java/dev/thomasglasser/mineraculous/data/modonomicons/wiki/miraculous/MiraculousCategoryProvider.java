package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;

public class MiraculousCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "miraculous";

    public MiraculousCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new GeneralEntryProvider(this).generate());
        add(new LadybugEntryProvider(this).generate());
        add(new CatEntryProvider(this).generate());
        add(new ButterflyEntryProvider(this).generate());
        add(new CustomizationEntryProvider(this).generate());
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
        return BookIconModel.create(Miraculous.createMiraculousStack(Miraculouses.LADYBUG));
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
