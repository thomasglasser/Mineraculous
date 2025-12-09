package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class KwamisEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "kwamis";

    public KwamisEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("summoning", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/summoning"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Summoning");
        pageText("""
                Kwamis are summoned by their (Miraculous)[category://miraculouses].
                When summoned,
                they will spin around in a ball of light.
                While they are in this form,
                they cannot be used to transform.
                """);

        page("transforming", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/transforming"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transforming");
        pageText("""
                In order to transform,
                the Kwami must be present and charged.
                When transforming, the kwami will speed towards the miraculous in a ball of light and disappear.
                """);

        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/charging"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                After detransforming,
                the kwami must be fed to recharge before it can be used to  transform again.
                To charge a kwami,
                feed it with any food item,
                or the kwami's preferred foods or treats.
                Treats will charge the kwami immediately,
                preferred foods have a 1 in 3 chance,
                and other foods foods have a 1 in 10 chance.
                """);

        page("item_form", () -> BookSpotlightPageModel.create()
                .withItem(Miraculous.createItemStack(MineraculousItems.KWAMI, registries().holderOrThrow(Miraculouses.LADYBUG)))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Item Form");
        pageText("""
                For better hiding, storage, and information,
                the kwami can be right clicked with an empty hand to enter the inventory in item form.
                While in the inventory, it can be hovered over to display information about it,
                such as its charged state and food items.
                It will eat preferred foods or treats in the inventory to recharge if enabled by the server config (default: true).
                When transforming with its miraculous, the kwami will exit the inventory to assist.
                """);

        page("transferring_and_renouncing", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/transferring_and_renouncing"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transferring and Renouncing");
        pageText("""
                To renounce a kwami and keep the miraculous, press the Renounce button (default: N) while holding the Miraculous in your hand.
                To transfer or renounce to someone else, right click the kwami with the Miraculous in hand.
                """);
    }

    @Override
    protected String entryName() {
        return "Kwamis";
    }

    @Override
    protected String entryDescription() {
        return "Magic creatures summoned by the Miraculouses.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousItems.KWAMI, registries().holderOrThrow(Miraculouses.CAT)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
