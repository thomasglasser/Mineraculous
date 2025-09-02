package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class CatEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "cat";

    public CatEntryProvider(CategoryProviderBase parent) {
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
                Charging the cat kwami requires [cheese](entry://food/cheese).
                By default, the normal food is cheese and the treat is camembert.
                These are configurable with the kwami_foods/cat and kwami_treats/cat [tags](entry://apis/tags).
                """);

        page("abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Abilities");
        pageText("""
                The Cat Miraculous has three abilities:
                - Cataclysm
                - Cat Vision
                - Passive Bad Luck
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
                They will drop Cataclysm Dust when killed.
                """);

        page("cat_vision", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/abilities/cat_vision.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cat Vision");
        pageText("""
                The Cat Vision ability is activated when you enter an area with very low light.
                It will cause your vision to become bright green.
                It will automatically deactivate when you enter an area with normal light.
                """);

        page("passive_unluck", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/abilities/passive_unluck.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Passive Bad Luck");
        pageText("""
                The Passive Bad Luck ability needs no activation.
                As the Cat Miraculous holder,
                you will automatically have the Bad Luck effect applied while transformed.
                This effect will result in worse loot when opening containers or fishing.
                The effect level scales as your Power Level increases.
                """);

        page("staff_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/abilities.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Staff Abilities");
        pageText("""
                The tool of the Cat Miraculous is a Cat Staff.
                It has five abilities:
                - Block
                - Perch
                - Spyglass
                - Throw
                - Travel
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block");
        pageText("""
                Block mode allows you to hold right click to spin your staff to make a shield that will block most attacks and projectiles.
                Holding the shield above your head will allow you to slow fall.
                """);

        page("perch", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/perch.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Perch");
        pageText("""
                Perch mode allows you to launch yourself upwards by right clicking.
                You can then right click again to go back down,
                left click to fall in the direction you are facing,
                or use the Ascend Tool (default: ⬆) and Descend Tool (default: ⬇) keys to change your height.
                """);

        page("spyglass", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/cat/staff/spyglass.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Spyglass");
        pageText("""
                Spyglass mode allows you to use your staff as a spyglass to get a closer look around the area you are in.
                You can use right click to look through the spyglass.
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
                Travel mode allows you to use your staff to launch yourself in the direction you are facing.
                You can then hold right click while in the air looking down to slow your fall or looking up to launch again.
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
        return BookIconModel.create(Miraculous.createMiraculousStack(registries().holderOrThrow(Miraculouses.CAT)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
