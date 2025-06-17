package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class CustomizationEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "customization";

    public CustomizationEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("screen", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/customization/screen.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Customization Screen");
        pageText("""
                The customization screen can be used to change the name and look of your miraculous persona.
                It can be opened with the '/miraculous <miraculous> customize' command.
                """);

        page("advanced", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Advanced Customization");
        pageText("""
                NOTE: The following features are only available if the server manually enables it.\\
                \\
                The mod can be further customized by putting files in a subfolder of the 'miraculouslooks' folder in the client's minecraft directory.\\
                A guide for customizing can be found [here](https://beta-jsons.thomasglasser.dev/guides/customization/).
                """);
    }

    @Override
    protected String entryName() {
        return "Customization";
    }

    @Override
    protected String entryDescription() {
        return "Per-player miraculous customization";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), registries().holderOrThrow(Miraculouses.LADYBUG)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
