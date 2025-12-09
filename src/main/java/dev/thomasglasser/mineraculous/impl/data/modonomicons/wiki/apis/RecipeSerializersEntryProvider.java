package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;

public class RecipeSerializersEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "recipe_serializers";

    public RecipeSerializersEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for recipes can be found online [here](https://beta-jsons.thomasglasser.dev/recipe/).
                """);

        page("oven_cooking", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Oven Cooking");
        add(context().pageText(), """
                The oven cooking recipe serializer is used with the Oven block.
                It has the same format as the other cooking recipes.
                """);

        page("transmute", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Transmute");
        add(context().pageText(), """
                Smelting, smoking, campfire cooking, and oven cooking recipes have transmute variants.
                They provide an item result instead of an ItemStack and transmute components to the result.
                """);
    }

    @Override
    protected String entryName() {
        return "Recipe Serializers";
    }

    @Override
    protected String entryDescription() {
        return "Custom recipe formats for new and existing processing blocks.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousBlocks.OVEN);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
