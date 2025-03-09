package dev.thomasglasser.mineraculous.data.modonomicons.wiki.kamikotizations;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class KamikotizationsCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "kamikotizations";

    public KamikotizationsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new GeneralEntryProvider(this).generate());
        add(new CustomizationEntryProvider(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Kamikotizations";
    }

    @Override
    protected String categoryDescription() {
        return "Powers given by the Butterfly Miraculous to normal players. Addons can add entries to this category describing their kamikotizations (see the Modonomicon Wiki for more info).";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.HEAD);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
