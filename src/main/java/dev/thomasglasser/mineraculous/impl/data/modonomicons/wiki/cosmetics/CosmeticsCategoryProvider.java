package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.cosmetics;

import com.klikli_dev.modonomicon.api.datagen.IndexModeCategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import net.minecraft.world.item.Items;

public class CosmeticsCategoryProvider extends IndexModeCategoryProvider {
    public static final String ID = "cosmetics";

    public CosmeticsCategoryProvider(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generateEntries() {
        add(new BetaEntry(this).generate());
        add(new DevEntry(this).generate());
    }

    @Override
    protected String categoryName() {
        return "Cosmetics";
    }

    @Override
    protected String categoryDescription() {
        return "Exclusive cosmetics for special mod community members.";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(Items.PLAYER_HEAD);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
