package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.itemstealingandbreaking;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BreakingEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "breaking";

    public BreakingEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        ItemStack broken = Items.OBSIDIAN.getDefaultInstance();
        broken.set(DataComponents.MAX_DAMAGE, 2);
        broken.setDamageValue(1);
        page("breaking", () -> BookSpotlightPageModel.create()
                .withItem(broken)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Breaking");
        pageText("""
                You can break items by pressing the Take/Break Item key (default: B).
                The item will be damaged based on its durability, block break time, or toughness.
                Some items cannot be broken, such as miraculous or unbreakable blocks and items.
                Tough items are defined with the "mineraculous:tough" [tag](entry://apis/tags).
                Unbreakable items have the "minecraft:unbreakable" data component.
                """);
    }

    @Override
    protected String entryName() {
        return "Breaking";
    }

    @Override
    protected String entryDescription() {
        return "Destroying items to release anything inside.";
    }

    @Override
    protected BookIconModel entryIcon() {
        ItemStack broken = Items.ELYTRA.getDefaultInstance();
        broken.setDamageValue(broken.getMaxDamage() - 1);
        return BookIconModel.create(broken);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
