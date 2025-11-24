package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
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
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/summoning.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Summoning");
        pageText("""
                Kwamis are summoned by their (Miraculous)[category://miraculouses].
                When summoned,
                they will spin around you in a ball of light.
                While they are in this form,
                you are unable to transform.
                """);

        page("transforming", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/transforming.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transforming");
        pageText("""
                In order to transform,
                your Kwami must be loaded in the world with you and charged.
                You can transform by pressing the Transform button (default: M).
                The kwami will then speed towards you in a ball of light and disappear into the Miraculous.
                """);

        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                After detransforming,
                your kwami must be fed to recharge before you can transform again.
                To charge a miraculous,
                feed the kwami with its food or treat.
                Treats will charge the kwami immediately, while foods may take a few tries.
                """);

        page("transferring_and_renouncing", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/kwamis/transferring_and_renouncing.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transferring and Renouncing");
        pageText("""
                To renounce a kwami and keep the miraculous, press the Renounce button (default: N) while holding the Miraculous in your hand.
                To transfer or renounce to someone else, right click the kwami with the Miraculous in your hand.
                """);
    }

    @Override
    protected String entryName() {
        return "Kwamis";
    }

    @Override
    protected String entryDescription() {
        return "Magic creatures summoned by the Miraculouses";
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
