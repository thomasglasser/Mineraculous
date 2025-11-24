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
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the ladybug kwami requires baked goods.
                By default, the normal food is bread and the treats are cookies and cakes.
                These are configurable with the kwami_foods/ladybug and kwami_treats/ladybug [tags](entry://apis/tags).
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
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/lucky_charm.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charm");
        pageText("""
                The Lucky Charm ability can be activated by pressing the Activate Power button (default: O) with the active yoyo in your hand.
                It will summon a lucky charm up to four blocks above you to help with your specific situation.
                The possible lucky charms are chosen from the loot table or list provided in the [data maps](entry://apis/data_maps).
                """);

        page("miraculous_ladybug", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/miraculous_ladybug.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Miraculous Ladybug");
        pageText("""
                The Miraculous Ladybug ability can be activated by pressing the Activate Power button (default: O) with the lucky charm in your hand.
                It will send magic ladybugs into the air and heal all damage caused by miraculous or kakikotization abilities related to the target.
                """);

        page("passive_luck", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/abilities/passive_luck.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Passive Luck");
        pageText("""
                The Passive Luck ability needs no activation.
                As the Ladybug Miraculous holder,
                you will automatically have the Luck effect applied while transformed.
                This effect helps get better loot when opening containers or fishing.
                The effect level scales as your Power Level increases.
                """);

        page("yoyo_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/abilities.png"))
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
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/attacking.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Attacking");
        pageText("""
                Because the tool is a Yoyo, you can't melee attack with it.
                Instead, left clicking will launch a damaging projectile in the direction you are facing.
                This will damage any entity or item you hit, releasing a [Kamiko](entry://flora_and_fauna/kamikos) if inside.
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block Mode");
        pageText("""
                Block mode allows you to hold right click to spin your yoyo to make a shield that will block most attacks and projectiles.
                Holding the shield above your head will allow you to slow fall.
                """);

        page("lasso", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/lasso.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lasso Mode");
        pageText("""
                Lasso mode allows you to use right click to throw your yoyo at any entity to keep it near you,
                even normally unleashable ones.
                You can also left click once the yoyo is attached to pull the entity back to you.
                You can control the length of the string using the Ascend Tool (default: Up Arrow) and Descend Tool (default: Down Arrow) keys.
                """);

        page("purify", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/purify.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Purify Mode");
        pageText("""
                Purify mode allows you to catch and purify [Kamikos](entry://flora_and_fauna/kamikos) with left click.
                You can capture as many as you want and right click to release them all at once.
                Holding right click will shoot entities upwards when released.
                """);

        page("spyglass", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/spyglass.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Spyglass Mode");
        pageText("""
                Spyglass mode allows you to use your yoyo as a spyglass to get a closer look around the area you are in.
                You can use right click to look through the spyglass.
                """);

        page("travel", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/ladybug/yoyo/travel.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Travel Mode");
        pageText("""
                Travel mode allows you to use your yoyo to swing on blocks and launch yourself into the air.
                You can use right click to throw and recall the yoyo and left click to launch yourself.
                """);

        page("lucky_charms", () -> BookSpotlightPageModel.create()
                .withItem(MineraculousItems.GREAT_SWORD)
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charms");
        pageText("""
                When targeting a Ladybug Miraculous holder,
                the lucky charms can be one of the following:
                - Great Sword
                """);
    }

    @Override
    protected String entryName() {
        return "Ladybug";
    }

    @Override
    protected String entryDescription() {
        return "The Miraculous of Creation";
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
