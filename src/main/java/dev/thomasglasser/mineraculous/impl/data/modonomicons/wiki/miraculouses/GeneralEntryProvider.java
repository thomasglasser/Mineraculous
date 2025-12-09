package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;

public class GeneralEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "general";

    public GeneralEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("obtaining", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/general/obtaining"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Obtaining");
        pageText("""
                The only way to obtain a miraculous currently is via the creative menu in the Miraculous tab.
                """);

        page("activating", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Activating");
        pageText("""
                To activate a miraculous, simply place it in the correct Curios slot.
                This can be done by right-clicking with the Miraculous in your hand or shift-clicking in the Curios inventory screen.
                """);

        page("transforming", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transforming");
        pageText("""
                To transform with a miraculous equipped, press the Transform button (default: U).
                """);

        page("using_abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using Abilities");
        pageText("""
                To use an ability with a miraculous equipped, press the Activate Power button (default: Y).
                """);

        page("timer", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculouses/general/timer"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Timer");
        pageText("""
                Once you perform your main ability, a detransformation timer will start.
                The length of this timer can be controlled by the server config.
                When the timer runs out, you will be automatically detransformed.
                You can track the remaining time by looking at the miraculous' flashes or listening to the miraculous' beeps.
                This timer will no longer apply after a certain amount of power uses.
                """);

        page("using_tool", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using the Tool");
        pageText("""
                Upon transformation, the tool will be automatically equipped in a curios slot or added to your inventory.
                This tool can be taken and returned to this slot when not activated by pressing the Open Item Radial Menu button (default: R).
                It can be activated by pressing the De/Activate Item button (default: C).
                When activated, the Open Item Radial Menu button (default: R) can be held to open the tool's radial menu and select a tool ability.
                """);

        page("toggling_buffs", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Toggling Buffs");
        pageText("""
                The Toggle Buffs key (default: `) can be pressed to toggle Miraculous effect levels.
                If enabled, Miraculous effect levels will be dependent on Power Level.
                If disabled, Miraculous effect levels will be capped to the starting levels.
                Whether the buffs are active upon transformation depends on the server config (true by default).
                """);
    }

    @Override
    protected String entryName() {
        return "General";
    }

    @Override
    protected String entryDescription() {
        return "Features of all miraculouses.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.head(), registries().holderOrThrow(Miraculouses.LADYBUG)));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
