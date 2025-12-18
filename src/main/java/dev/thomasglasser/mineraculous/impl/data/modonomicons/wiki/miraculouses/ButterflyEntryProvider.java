package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class ButterflyEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "butterfly";

    public ButterflyEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/charging"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the butterfly kwami requires flowers.
                By default, the preferred food is small flowers and the treat is [hibiscus](entry://flora_and_fauna/flowers).
                These are configurable with the kwami_preferred_foods/butterfly and kwami_treats/butterfly [tags](entry://apis/tags).
                """);

        page("abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Abilities");
        pageText("""
                The Butterfly Miraculous has 3 abilities:
                - Kamikotization
                - Kamiko Control
                - Kamikotized Communication
                """);

        page("kamikotization", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/abilities/kamikotization"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamikotization");
        pageText("""
                The Kamikotization ability can be activated by pressing the Activate Power button (default: Y) with no Kamikos nearby or stored.
                It will cause particles to come from the performer's hand.
                It will then convert any non-player entity into a Kamiko on interaction.
                """);

        page("kamiko_control", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/abilities/kamiko_control"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamiko Control");
        pageText("""
                The Kamiko Control ability can be activated by pressing the Activate Power button (default: Y) with a Kamiko nearby.
                It will cause a mask to appear on the performer's face and will allow seeing through the eyes of the Kamiko.
                The number keys then allow selecting a target that the Kamiko will fly to.
                Once it reaches the target, it will open the Kamikotization Selection Screen and allow kamikotizing the target.
                """);

        page("kamikotized_communication", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/abilities/kamikotized_communication"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamikotized Communication");
        pageText("""
                The Kamikotized Communication ability can be activated by pressing the Activate Power button (default: Y) with a kamikotized entity nearby.
                It will cause a mask to appear on the performer's and target's face and will allow spectation, private chat, remote damage, and kamikotization revocation.
                """);

        page("cane_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/cane/abilities"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cane Abilities");
        pageText("""
                The tool of the Butterfly Miraculous is a Butterfly Cane.
                It has five abilities:
                - Blade
                - Block
                - Kamiko Store
                - Spyglass
                - Throw
                """);

        page("blade", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/cane/blade"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Blade");
        pageText("""
                Blade mode unsheathes a rapier-like blade that can be used to attack entities.
                It can also be thrown, damaging any entity or item hit, and releasing a Kamiko if inside.
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block");
        pageText("""
                Block mode allows holding right click to spin the butterfly cane into a shield that will block most attacks and projectiles.
                Holding the shield overhead allows slow fall.
                """);

        page("kamiko_store", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/cane/kamiko_store"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamiko Store");
        pageText("""
                Kamiko Store mode allows right clicking to store or release a single Kamiko.
                """);

        page("spyglass", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/cane/spyglass"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Spyglass");
        pageText("""
                Spyglass mode allows using the cane as a spyglass to get a closer look at things.
                It is activated by holding right click.
                """);

        page("throw", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/butterfly/cane/throw"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Throw");
        pageText("""
                Throw mode allows right clicking to throw the cane.
                This will damage any entity or item hit, releasing a Kamiko if inside.
                """);
    }

    @Override
    protected String entryName() {
        return "Butterfly";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Transmission.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(registries().holderOrThrow(Miraculouses.BUTTERFLY)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
