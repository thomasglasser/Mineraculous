package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.kamikotizations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class GeneralEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kamikotizations_general";

    public GeneralEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("receiving", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("kamikotizations/general/receiving"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Receiving");
        pageText("""
                The only way to receive a kamikotization is from a Kamiko from the [Butterfly Miraculous](entry://miraculouses/butterfly).
                The owner of the kamiko sends it to a target to access the contents of the target's inventory.
                They will then choose a kamikotization to apply based on the items in the target's inventory.
                The target has the option to accept the kamikotizations via a button or can reject the kamikotization with the Escape key if the server allows it.
                """);

        page("rejecting", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Rejecting");
        pageText("""
                Kamikotizations can be rejected by the target if the server allows it by pressing the Reject Kamikotization key (default: K).
                This will release the kamiko from the item and remove the kamikotization and powers.
                """);

        page("revoking", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("kamikotizations/general/revoking"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Revoking");
        pageText("""
                Kamikotizations are revoked when the kamikotized item is destroyed or when the [Butterfly Miraculous](entry://miraculouses/butterfly) holder chooses to revoke the kamikotization.
                This will release the kamiko from the item and remove the kamikotization and powers.
                """);

        page("using_tool", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using the Tool or Ability");
        pageText("""
                When kamikotized, the kamikotized item will either be turned into a tool or the target will be given an ability to use on key press (default: O).
                This tool and ability will be different depending on the kamikotization;
                refer to the [Kamikotizations](category://kamikotizations) category for more info.
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
        return BookIconModel.create(MineraculousArmors.KAMIKOTIZATION.head());
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
