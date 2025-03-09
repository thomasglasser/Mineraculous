package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class LadybugEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "ladybug";

    public LadybugEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                Charging the ladybug kwami requires baked goods.
                By default, the normal food is bread and the treats are cookies and cakes.
                These are configurable with the kwami_foods/ladybug and kwami_treats/ladybug [tags](entry://apis/tags).
                """);

        page("yoyo_abilities", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/yoyo/abilities.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Yoyo Abilities");
        pageText("""
                The tool of the Ladybug Miraculous is a Yoyo.
                It has a unique attack functionality and 4 ability modes:
                - Block
                - Lasso
                - Purify
                - Travel
                """);

        page("attack", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/yoyo/attack.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Attacking");
        pageText("""
                Because the tool is a Yoyo, you can't melee attack with it.
                Instead, left clicking in any mode will launch a damaging projectile in the direction you are facing.
                This will damage any entity or item you hit, releasing a [Kamiko](entry://flora_and_fauna/kamikos) if inside.
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Block Mode");
        pageText("""
                Block mode allows you to hold right click to spin your yoyo to make a shield that will block most attacks and projectiles.
                """);

        page("lasso", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/yoyo/lasso.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lasso Mode");
        pageText("""
                Lasso mode allows you to use right click to throw your yoyo at entities to bind them together in one place.
                You can also left click once the yoyo is attached to entities to pull them back to you.
                """);

        page("purify", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/yoyo/purify.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Purify Mode");
        pageText("""
                Purify mode allows you to catch and purify powered [Kamikos](entry://flora_and_fauna/kamikos) with left click.
                You can capture as many as you want and right click to release them all at once.
                """);

        page("travel", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/yoyo/travel.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Travel Mode");
        pageText("""
                Travel mode allows you to use your yoyo to swing on blocks and launch yourself into the air.
                You can use right click to throw and catch the yoyo and left click to launch yourself.
                """);

        page("abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Abilities");
        pageText("""
                The Ladybug miraculous has 2 abilities:
                - Lucky Charm
                - Miraculous Ladybug
                """);

        page("lucky_charm", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/abilities/lucky_charm.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Lucky Charm");
        pageText("""
                The Lucky Charm ability can be activated by pressing the Activate Power button (default: O) with the active yoyo in your hand.
                It will grant you a lucky charm to help with your specific situation.
                The target of the lucky charm will be determined by the entity you last attacked or were attacked by.
                The possible lucky charms are chosen from the loot table or list provided in the [data maps](entry://apis/data_maps).
                """);

        page("miraculous_ladybug", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/abilities/miraculous_ladybug.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Miraculous Ladybug");
        pageText("""
                The Miraculous Ladybug ability can be activated by pressing the Activate Power button (default: O) with the lucky charm in your hand.
                It will launch the lucky charm into the air with magic ladybugs and heal all damage caused by miraculous or kakikotization abilities related to the target.
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
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.LADYBUG));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
