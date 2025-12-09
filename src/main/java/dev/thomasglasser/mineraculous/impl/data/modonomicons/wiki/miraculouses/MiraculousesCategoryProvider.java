package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;

public class MiraculousesCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "miraculouses";

    public MiraculousesCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new GeneralEntryProvider(this).generate());
        add(new KwamisEntryProvider(this).generate());
        add(new CommandEntryProvider(this).generate());
        add(new LadybugEntryProvider(this).generate());
        add(new CatEntryProvider(this).generate());
        add(new ButterflyEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Miraculouses";
    }

    @Override
    protected String categoryDescription() {
        return "Magical jewels that provide amazing abilities.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(registries().holderOrThrow(Miraculouses.LADYBUG)));
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
