package dev.thomasglasser.mineraculous.data.modonomicons.wiki;

import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import com.klikli_dev.modonomicon.book.BookDisplayMode;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis.ApisCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.decorations.DecorationsCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.dependencies.DependenciesCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.expansions.ExpansionsCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.floraandfauna.FloraAndFaunaCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.food.FoodCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.itemstealingandbreaking.ItemStealingAndBreakingCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.kamikotizations.KamikotizationsCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous.MiraculousCategoryProvider;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.support.SupportCategoryProvider;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;

public class WikiBookSubProvider extends SingleBookSubProvider {
    public static final String ID = "wiki";

    public WikiBookSubProvider(BiConsumer<String, String> lang) {
        super(ID, Mineraculous.MOD_ID, lang);
    }

    protected void registerDefaultMacros() {}

    @Override
    protected void generateCategories() {
        add(new MiraculousCategoryProvider(this).generate());
        add(new KamikotizationsCategoryProvider(this).generate());
        add(new ItemStealingAndBreakingCategoryProvider(this).generate());
        add(new FloraAndFaunaCategoryProvider(this).generate());
        add(new FoodCategoryProvider(this).generate());
        add(new DecorationsCategoryProvider(this).generate());
        add(new ExpansionsCategoryProvider(this).generate());
        add(new DependenciesCategoryProvider(this).generate());
        add(new ApisCategoryProvider(this).generate());
        add(new SupportCategoryProvider(this).generate());
    }

    @Override
    protected BookModel additionalSetup(BookModel book) {
        book = super.additionalSetup(book);
        return book
                .withDisplayMode(BookDisplayMode.INDEX)
                .withModel(Mineraculous.Dependencies.MODONOMICON.modLoc("modonomicon_red"))
                .withCreativeTab(MineraculousCreativeModeTabs.MINERACULOUS.getId())
                .withGenerateBookItem(true);
    }

    @Override
    protected String bookName() {
        return "Mineraculous Wiki";
    }

    @Override
    protected String bookTooltip() {
        return "Your guide to the world of Mineraculous";
    }

    @Override
    protected String bookDescription() {
        return """
                Thank you for installing Mineraculous!
                Within these pages is everything you'll ever need to know about the mod,
                from how to obtain a miraculous to how to use the many powers it grants you.
                Check back here when new updates come out to see the changes!
                """;
    }

    public static ResourceLocation wikiTexture(String path) {
        return Mineraculous.modLoc("textures/modonomicon/wiki/" + path);
    }
}
