package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;

public class MiraculousEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "miraculous";

    public MiraculousEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for miraculous can be found online [here](https://beta-jsons.thomasglasser.dev/mineraculous/miraculous/).
                """);

        page("guide", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Guide");
        add(context().pageText(), """
                A guide for creating miraculous can be found [here](https://beta-jsons.thomasglasser.dev/guides/miraculous/).
                """);
    }

    @Override
    protected String entryName() {
        return "Miraculous";
    }

    @Override
    protected String entryDescription() {
        return "Jewels that provide special abilities and fancy a super suit to the holder";
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
