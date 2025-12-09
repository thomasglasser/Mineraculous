package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class LadybugEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "ladybug";

    public LadybugEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/charging"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the ladybug kwami requires baked goods.
                By default, the preferred food is bread and the treats are macarons, cookies, and cakes.
                These are configurable with the kwami_preferred_foods/ladybug and kwami_treats/ladybug [tags](entry://apis/tags).
                """);

        page("abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Abilities");
        pageText("""
                The Ladybug miraculous has three abilities:
                - Lucky Charm
                - Miraculous Ladybug
                - Passive Luck
                """);

        page("lucky_charm", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/lucky_charm"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charm");
        pageText("""
                The Lucky Charm ability can be activated by pressing the Activate Power button (default: Y) with the active yoyo in hand.
                This will summon a lucky charm item at the performer position.
                If the tool is a special lucky charm summoning item, it can override the summon position.
                The possible lucky charms are chosen from the loot table or list provided in the lucky charm [data maps](entry://apis/data_maps).
                It will be tied to the relevant target used to determine the item.
                """);

        page("miraculous_ladybug", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/miraculous_ladybug"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Miraculous Ladybug");
        pageText("""
                The Miraculous Ladybug ability can be activated by pressing the Activate Power button (default: Y) with the lucky charm in hand.
                It will send the lucky charm into the air and summon magic ladybugs that fly around and heal all damage caused by miraculous or kakikotization abilities related to the target,
                or the summoner of the Miraculous Ladybug if no target is specified.
                There are many server and client config options for customization of this ability,
                including one for altering the performance impact.
                """);

        page("passive_luck", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/passive_luck"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Passive Luck");
        pageText("""
                The Passive Luck ability needs no activation.
                The Ladybug Miraculous holder automatically has the Luck effect applied while transformed.
                This effect produces better loot when opening containers or fishing.
                The effect level scales as Power Level increases.
                """);

        page("yoyo_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/abilities"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Yoyo Abilities");
        pageText("""
                The tool of the Ladybug Miraculous is the Ladybug Yoyo.
                It has a unique attack functionality and five ability modes:
                - Block
                - Lasso
                - Purify
                - Spyglass
                - Travel
                """);

        page("attacking", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/attacking"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Attacking");
        pageText("""
                Because the tool is a Yoyo, it can't melee attack.
                Instead, left clicking will launch a damaging projectile in the direction the user is facing.
                This will damage any entity or item hit, releasing a Kamiko if inside.
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block Mode");
        pageText("""
                Block mode allows holding right click to spin the yoyo in a shield that will block most attacks and projectiles.
                Holding the shield upwards allows slow fall.
                """);

        page("lasso", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/lasso"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lasso Mode");
        pageText("""
                Lasso mode allows right clicking to throw the yoyo at any entity to keep it nearby,
                even normally unleashable ones.
                The yoyo can also be left clicked once an entity is attached to pull it towards the holder.
                The length of the string can be controlled using the Ascend Tool (default: X) and Descend Tool (default: Z) keys.
                """);

        page("purify", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/purify"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Purify Mode");
        pageText("""
                Purify mode allows capturing and purify Kamikos with left click.
                There is no limit to how many Kamikos can be captured,
                and right clicking will release all stored Kamikos at once,
                launching them upwards.
                When no Kamikos are stored, right clicking causes the yoyo to spin as a shield.
                """);

        page("spyglass", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/spyglass"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Spyglass Mode");
        pageText("""
                Spyglass mode allows using the yoyo as a spyglass to get a closer look at things.
                It is activated by holding right click.
                """);

        page("travel", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/travel"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Travel Mode");
        pageText("""
                Travel mode allows using the yoyo to swing on blocks and launch into the air.
                Right click throws and recalls the yoyo and left click launches the user.
                """);

        page("lucky_charms", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousItems.GREAT_SWORD)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charms");
        pageText("""
                When targeting a Ladybug Miraculous holder,
                the lucky charm can be one of the following:
                - Great Sword
                """);
    }

    @Override
    protected String entryName() {
        return "Ladybug";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Creation.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(registries().holderOrThrow(Miraculouses.LADYBUG)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
