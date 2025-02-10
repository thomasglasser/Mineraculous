package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;

public class AbilitiesEntry extends IndexModeEntryProvider {
    private static final String ID = "abilities";

    public AbilitiesEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for abilities can be found online [here](https://snapshot-jsons.thomasglasser.dev/mineraculous/ability/).
                """);

        page("type", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Type");
        add(context().pageText(), """
                Abilities have a type that determines how they work and what fields the json should have.
                Some examples include:
                - mineraculous:night_vision
                - mineraculous:random_spread
                - mineraculous:context_aware
                - mineraculous:apply_infinite_effects_or_destroy
                - mineraculous:replace_items_in_hand
                - mineraculous:right_hand_particles
                - mineraculous:lucky_charm_world_recovery\\
                The 'drag' and 'context_aware' abilities allow for more complex abilities.
                """);
    }

    @Override
    protected String entryName() {
        return "Abilities";
    }

    @Override
    protected String entryDescription() {
        return "Supernatural abilities that can be used by Miraculous holders or Kamikotizations";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.CATACLYSM_DUST);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
