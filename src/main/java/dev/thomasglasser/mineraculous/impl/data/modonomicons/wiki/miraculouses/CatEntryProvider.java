package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.tommylib.api.tags.ConventionalItemTags;
import net.minecraft.world.item.crafting.Ingredient;

public class CatEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "cat";

    public CatEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/charging"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the cat kwami requires [cheese](entry://food/cheese).
                By default, the normal food is any kind of cheese and the treat is camembert.
                These are configurable with the kwami_preferred_foods/cat and kwami_treats/cat [tags](entry://apis/tags).
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
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/abilities/cataclysm"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cataclysm");
        pageText("""
                The Cataclysm ability can be activated by pressing the Activate Power button (default: Y).
                It will cause particles to come from the performer's hand and can be dragged for 1 second after use begins.
                It can be used on items, blocks, and entities.
                Items will be converted to Cataclysm Dust.
                Blocks will slowly convert themselves and adjacent blocks to Cataclysm Blocks,
                which drop Cataclysm Dust when broken with Silk Touch.
                Entities will be given the Cataclysmed effect that will slowly kill them,
                converting drops to Cataclysm Dust.
                This is additive, so interacting with an entity multiple times will increase the Cataclysmed effect level.
                It is possible to block Cataclysm with a shield,
                which will apply it to the shield item instead of the target.
                """);

        page("cat_vision", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/abilities/cat_vision"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Cat Vision");
        pageText("""
                The Cat Vision ability is toggleable with the Toggle Night Vision key (default: V).
                It will cause vision to become bright and green.
                """);

        page("passive_unluck", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/abilities/passive_unluck"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Passive Bad Luck");
        pageText("""
                The Passive Bad Luck ability needs no activation.
                The Cat Miraculous holder automatically has the Bad Luck effect applied while transformed.
                This effect produces worse loot when opening containers or fishing.
                The effect level scales as Power Level increases.
                """);

        page("staff_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/staff/abilities"))
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
                Block mode allows holding right click to spin the staff in a shield that will block most attacks and projectiles.
                Holding the shield upwards allows slow fall.
                """);

        page("perch", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/staff/perch"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Perch");
        pageText("""
                Perch mode allows launching upwards by right clicking.
                Right click can be pressed again to go back down,
                or left click will make the staff fall in the facing direction,
                or use the Ascend Tool (default: X) and Descend Tool (default: Z) keys to change staff height.
                """);

        page("spyglass", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/staff/spyglass"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Spyglass");
        pageText("""
                Spyglass mode allows using the yoyo as a spyglass to get a closer look at things.
                It is activated by holding right click.
                """);

        page("throw", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/staff/throw"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Throw");
        pageText("""
                Throw mode allows right clicking to throw the staff.
                This will damage any entity or item you hit, releasing a Kamiko if inside.
                """);

        page("travel", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/cat/staff/travel"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Travel");
        pageText("""
                Travel mode allows using the staff to launch in the facing direction.
                Right click can be held while in the air looking down to slow fall or looking up to launch again.
                """);

        page("lucky_charms", () -> BookSpotlightPageModel.create()
                .withItem(Ingredient.of(ConventionalItemTags.SHIELD_TOOLS))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charms");
        pageText("""
                When targeting a Cat Miraculous holder,
                the lucky charms can be one of the following:
                - Any Shield
                """);
    }

    @Override
    protected String entryName() {
        return "Cat";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Destruction.";
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
