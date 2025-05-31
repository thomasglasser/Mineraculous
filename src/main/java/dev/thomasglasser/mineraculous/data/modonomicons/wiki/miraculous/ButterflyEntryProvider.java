package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;

public class ButterflyEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "butterfly";

    public ButterflyEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the butterfly kwami requires flowers.
                By default, the normal food is small flowers and the treats are [hibiscus](entry://flora_and_fauna/flowers).
                These are configurable with the kwami_foods/butterfly and kwami_treats/butterfly [tags](entry://apis/tags).
                """);

        page("cane_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/cane/abilities.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cane Abilities");
        pageText("""
                The tool of the Butterfly Miraculous is a Butterfly Cane.
                It has 4 abilities:
                - Blade
                - Block
                - Kamiko Store
                - Throw
                """);

        page("blade", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/cane/blade.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Blade");
        pageText("""
                Blade mode unsheathes a rapier-like blade that can be used to attack entities.
                You can also right click to throw the blade, damaging any entity or item you hit, and releasing a [Kamiko](entry://flora_and_fauna/kamikos) if inside.
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block");
        pageText("""
                Block mode allows you to hold right click to spin your butterfly cane to make a shield that will block most attacks and projectiles.
                """);

        page("kamiko_store", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/cane/kamiko_store.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamiko Store");
        pageText("""
                Kamiko Store mode allows you to right click to store a powered [Kamiko](entry://flora_and_fauna/kamikos).
                You can then right click again to release the [Kamiko](entry://flora_and_fauna/kamikos).
                """);

        page("throw", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/cane/throw.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Throw");
        pageText("""
                Throw mode allows you to right click to throw your butterfly cane.
                This will damage any entity or item you hit, releasing a [Kamiko](entry://flora_and_fauna/kamikos) if inside.
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
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/abilities/kamikotization.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamikotization");
        pageText("""
                The Kamikotization ability can be activated by pressing the Activate Power button (default: O) with no powered [Kamikos](entry://flora_and_fauna/kamikos) nearby.
                It will cause particles to come from your hand.
                You can then interact with an unpowered [Kamiko](entry://flora_and_fauna/kamikos) to power and tame it.
                """);

        page("kamiko_control", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/abilities/kamiko_control.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamiko Control");
        pageText("""
                The Kamiko Control ability can be activated by pressing the Activate Power button (default: O) with a powered [Kamiko](entry://flora_and_fauna/kamikos) nearby.
                It will cause a mask to appear on your face and will allow you to see through the eyes of the [Kamiko](entry://flora_and_fauna/kamikos).
                You can then press the number keys to select a target that the [Kamiko](entry://flora_and_fauna/kamikos) will fly to.
                Once it reaches the target, it will open the Kamikotization Selection Screen and allow you to kamikotize the target.
                """);

        page("kamikotized_communication", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/butterfly/abilities/kamikotized_communication.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamikotized Communication");
        pageText("""
                The Kamikotized Communication ability can be activated by pressing the Activate Power button (default: O) with a kamikotized player nearby.
                It will cause a mask to appear on your face and the player's face and will allow you to see through their eyes.
                You can then speak to the player or revoke their kamikotization.
                """);
    }

    @Override
    protected String entryName() {
        return "Butterfly";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Transmission";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(Miraculouses.BUTTERFLY));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
