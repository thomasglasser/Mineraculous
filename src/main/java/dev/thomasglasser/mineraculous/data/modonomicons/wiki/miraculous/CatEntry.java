package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class CatEntry extends IndexModeEntryProvider {
    private static final String ID = "cat";

    public CatEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the cat kwami requires [cheeses](entry://food/cheese).
                By default, the normal food is cheese and the treat is camembert.
                These are configurable with the kwami_foods/cat and kwami_treats/cat [tags](entry://apis/tags).
                """);

        page("staff_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/abilities.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Staff Abilities");
        pageText("""
                The tool of the Cat Miraculous is a Staff.
                It has 4 abilities:
                - Block
                - Perch
                - Throw
                - Travel
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block");
        pageText("""
                Block mode allows you to hold right click to spin your staff to make a shield that will block most attacks and projectiles.
                """);

        page("perch", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/perch.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Perch");
        pageText("""
                Perch mode allows you to go up and down on your staff by right clicking and looking in the direction you want to go.
                You can hold shift to remain in your current position.
                """);

        page("throw", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/throw.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Throw");
        pageText("""
                Throw mode allows you to right click to throw your staff at entities.
                This will damage any entity or item you hit, releasing a [Kamiko](entry://flora_and_fauna/kamikos) if inside.
                """);

        page("travel", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/travel.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Travel");
        pageText("""
                Travel mode allows you to use your staff to launch yourself into the air.
                You can use right click to launch yourself in the direction you are facing.
                """);

        page("abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Abilities");
        pageText("""
                The Cat Miraculous has 2 abilities:
                - Cataclysm
                - Cat Vision
                """);

        page("cataclysm", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/abilities/cataclysm.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cataclysm");
        pageText("""
                The Cataclysm ability can be activated by pressing the Activate Power button (default: O).
                It will cause particles to come from your hand and can be dragged for 1 second after you start using it.
                You can interact with items, blocks, and entities.
                Items will be converted to Cataclysm Dust.
                Blocks will be converted to Cataclysm Blocks, which drop Cataclysm Dust.
                Entities will be given the Cataclysmed effect that will slowly kill them.
                They will also drop Cataclysm Dust.
                """);

        page("cat_vision", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/abilities/cat_vision.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cat Vision");
        pageText("""
                The Cat Vision ability is activated when you enter an area with very low light.
                It will cause your vision to become bright, green, and pixelated.
                It will automatically deactivate when you enter an area with normal light.
                """);
    }

    @Override
    protected String entryName() {
        return "Cat";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Destruction";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.CAT));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
