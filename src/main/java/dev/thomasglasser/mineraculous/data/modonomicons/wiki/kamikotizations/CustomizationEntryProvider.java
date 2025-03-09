package dev.thomasglasser.mineraculous.data.modonomicons.wiki.kamikotizations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class CustomizationEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikotizations_customization";

    public CustomizationEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("overriding", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Overriding");
        pageText("""
                NOTE: The following features are only available if the server manually enables it.\\
                \\
                The look of your kamikotizations can be further customized by putting files in the 'kamikotizations' subfolder of the 'miraculouslooks' folder in the client's minecraft directory.\\
                A guide for customizing can be found [here](https://snapshot-jsons.thomasglasser.dev/guides/customization/).
                """);
    }

    @Override
    protected String entryName() {
        return "Customization";
    }

    @Override
    protected String entryDescription() {
        return "Per-player kamikotization customization";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.HEAD);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
