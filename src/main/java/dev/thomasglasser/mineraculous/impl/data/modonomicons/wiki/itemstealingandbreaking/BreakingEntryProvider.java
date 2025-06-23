package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.itemstealingandbreaking;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class BreakingEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "breaking";

    public BreakingEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("breaking", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("item_stealing_and_breaking/breaking.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Breaking");
        pageText("""
                You can break items by pressing the Take/Break Item key (default: I).
                The item will be damaged based on its max damage, block durability, or toughness.
                Some items cannot be broken, such as miraculous or unbreakable blocks and items.
                Tough items are defined with the 'tough' [tag](entry://apis/tags).
                """);
    }

    @Override
    protected String entryName() {
        return "Breaking";
    }

    @Override
    protected String entryDescription() {
        return "Destroying items";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.COAL);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
