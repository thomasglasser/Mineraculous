package dev.thomasglasser.mineraculous.data.modonomicons.wiki.kamikotizations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class GeneralEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikotizations_general";

    public GeneralEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("receiving", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Receiving");
        pageText("""
                The only way to receive a kamikotization is from a powered [Kamiko](entry://flora_and_fauna/kamikos).
                The owner of the kamiko will choose a kamikotization with powers for you based on the items in your inventory.
                It will then send the kamiko to you and it will enter that item.
                You have the option to accept the powers, or you can reject the kamikotization if the server allows it.
                """);

        page("revoking", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Revoking");
        pageText("""
                Kamikotizations are revoked when the kamikotized item is destroyed or when the [Butterfly Miraculous](entry://miraculous/butterfly) holder chooses to revoke the kamikotization.
                This will release the kamiko from the item and remove the kamikotization and powers.
                """);

        page("using_tool", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using the Tool or Ability");
        pageText("""
                When you are kamikotized, your kamikotized item will either be turned into a tool or you will be given an ability to use on key press (default: O).
                This tool and ability will be different depending on the kamikotization, so refer to the [Kamikotizations](category://kamikotizations) category for more info.
                """);
    }

    @Override
    protected String entryName() {
        return "General";
    }

    @Override
    protected String entryDescription() {
        return "Features of all kamikotizations";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.HEAD.get());
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
