package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class GeneralEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "general";

    public GeneralEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("obtaining", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/general/obtaining.png"))
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
                To activate a miraculous, simply place it in the correct Curios slot. This can be done by right-clicking with the Miraculous in your hand.
                """);

        page("transforming", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transforming");
        pageText("""
                To transform with a miraculous equipped, press the Transform button (default: M).
                """);

        page("using_tool", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using the Tool");
        pageText("""
                When you transform, the tool will be automatically equipped in a curios slot (if applicable) or be added to your inventory.
                You can take and return the tool when not activated to this slot by pressing the Equip/Return Tool button (default: H).
                You can also activate the tool by pressing the Activate Tool button (default: U).
                When activated, you can hold the Open Tool Wheel button (default: H) to open the tool wheel and select a tool ability.
                """);

        page("using_abilities", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Using Abilities");
        pageText("""
                To use an ability with a miraculous equipped, press the Activate Power button (default: O).
                """);

        page("timer", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/general/timer.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Timer");
        pageText("""
                Once you use your main ability, a 5 minute timer will start.
                When the timer runs out, you will be automatically detransformed.
                You can track the remaining time by looking at the miraculous or listening to the frequency of the beeps.
                """);

        page("charging", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/ladybug/charging.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Charging");
        pageText("""
                To charge a miraculous, feed the kwami with a food or treat.
                Treats will charge the kwami immediately, while foods may take a few tries.
                """);

        page("transferring_and_renouncing", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/general/transferring_and_renouncing.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Transferring and Renouncing");
        pageText("""
                To renounce and keep a miraculous, press the Renounce button (default: O) while holding the Miraculous in your hand.
                To transfer or renounce to someone else, right click the kwami with the Miraculous in your hand.
                """);
    }

    @Override
    protected String entryName() {
        return "General";
    }

    @Override
    protected String entryDescription() {
        return "Features of all miraculous";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), Miraculouses.LADYBUG));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
