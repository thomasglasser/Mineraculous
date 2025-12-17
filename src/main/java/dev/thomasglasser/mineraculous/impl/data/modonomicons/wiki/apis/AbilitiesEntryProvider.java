package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;

public class AbilitiesEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "abilities";

    public AbilitiesEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for abilities can be found online [here](https://beta-jsons.thomasglasser.dev/mineraculous/ability/).
                """);

        page("type", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Type");
        add(context().pageText(), """
                Abilities have a type that determines how they work and what fields the json should have.
                Some examples include:
                - mineraculous:apply_effects_or_destroy
                - mineraculous:automatic_night_vision
                - mineraculous:context_dependent
                - mineraculous:continuous
                - mineraculous:convert_and_tame
                - mineraculous:passive_effects
                - mineraculous:replace_adjacent_blocks
                - mineraculous:replace_item_in_main_hand
                - mineraculous:revert_lucky_charm_targets_ability_effects
                - mineraculous:right_hand_particles
                - mineraculous:spectate_entity
                - mineraculous:summon_target_dependent_lucky_charm\\
                The "context_dependent" and "continuous" types allow for more complex abilities.
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
