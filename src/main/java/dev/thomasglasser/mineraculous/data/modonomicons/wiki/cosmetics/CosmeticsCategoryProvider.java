package dev.thomasglasser.mineraculous.data.modonomicons.wiki.cosmetics;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class CosmeticsCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "cosmetics";

    public CosmeticsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        // Miraculous looks
        // Suit looks
        // Kamikotization looks
    }

    @Override
    protected String categoryName() {
        return "Cosmetics";
    }

    @Override
    protected String categoryDescription() {
        return "Per-player customization for Miraculous and Kamikotizations.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), MineraculousMiraculous.LADYBUG));
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
