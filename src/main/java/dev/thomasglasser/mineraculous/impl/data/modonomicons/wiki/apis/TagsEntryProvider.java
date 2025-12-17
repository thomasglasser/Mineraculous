package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;

public class TagsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "tags";

    public TagsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generators", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generators");
        add(context().pageText(), """
                Generators for vanilla tags can be found online [here](https://beta-jsons.thomasglasser.dev/generators/) and mod tags can be found online [here](https://beta-jsons.thomasglasser.dev/partners/).
                """);

        page("block", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Block");
        add(context().pageText(), """
                There are three mod tags and one common tag used by the mod:
                - "mineraculous:cataclysm_immune": Blocks that cataclysm cannot apply to.
                - "mineraculous:cheese_blocks": Blocks of normal cheese added by the mod.
                - "mineraculous:camembert_blocks": Blocks of camembert cheese added by the mod.
                - "c:foods/cheese_blocks": Blocks from any mod that can be considered cheese and food.
                """);

        page("damage_type", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Damage Type");
        add(context().pageText(), """
                There are two mod tags used by the mod:
                - "mineraculous:hurts_kamikos": Damage types that can damage Kamikos.
                - "mineraculous:is_cataclysm": Damage types that can be considered cataclysm.
                """);

        page("entity_type", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Entity Type");
        add(context().pageText(), """
                There are two mod tags and one common tag used by the mod:
                - "mineraculous:cataclysm_immune": Entity types that cataclysm cannot apply to.
                - "mineraculous:ladybug_yoyo_extended_range": Entity types that can be hit from a further distance by the Ladybug Yoyo.
                - "c:butterflies": Entity types from any mod that can be considered butterflies.
                """);

        page("item", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Item");
        add(context().pageText(), """
                There are seventeen mod tags and two common tags used by the mod:
                - "mineraculous:kwami_preferred_foods/butterfly": Items that can be used to have a better chance to charge the butterfly kwami.
                - "mineraculous:kwami_treats/butterfly": Items that can be used to immediately charge the butterfly kwami.
                - "mineraculous:kwami_preferred_foods/cat": Items that can be used to have a better chance to charge the cat kwami.
                - "mineraculous:kwami_treats/cat": Items that can be used to immediately charge the cat kwami.
                - "mineraculous:kwami_preferred_foods/ladybug": Items that can be used to have a better chance to charge the ladybug kwami.
                - "mineraculous:kwami_treats/ladybug": Items that can be used to immediately charge the ladybug kwami.
                - "mineraculous:cheese": Items that are normal cheese added by the mod.
                - "mineraculous:cheese_blocks": Items that are normal cheese blocks added by the mod.
                - "mineraculous:camembert": Items that are camembert cheese added by the mod.
                - "mineraculous:camembert_blocks": Items that are camembert cheese blocks added by the mod.
                - "mineraculous:cataclysm_immune": Items that cataclysm cannot apply to.
                - "mineraculous:tough": Items that take two tries to break if they do not have a max damage value.
                - "mineraculous:lucky_charm_shader_immune": Items that do not have a visual change when given as a lucky charm.
                - "mineraculous:shooting_projectiles": Projectiles that shoot down as a Lucky Charm instead of dropping normally.
                - "mineraculous:generic_lucky_charms": Lucky charm options when no specific pool is specified.
                - "mineraculous:warden_distractors": Items passed in a Warden lucky charm to distract it.
                - "mineraculous:kamikotization_immune": Items that cannot be used for Kamikotization.
                - "c:foods/cheeses": Items from any mod that can be considered cheese and food.
                - "c:foods/cheese_blocks": An item copy of the "c:foods/cheese_blocks" block tag.
                """);

        page("miraculous", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Miraculous");
        add(context().pageText(), """
                There are three mod tags used by the mod:
                - "mineraculous:can_use_butterfly_cane": Miraculouses that can use and store data for the Butterfly Cane.
                - "mineraculous:can_use_cat_staff": Miraculouses that can use and store data for the Cat Staff.
                - "mineraculous:can_use_ladybug_yoyo": Miraculouses that can use and store data for the Ladybug Yoyo.
                """);
    }

    @Override
    protected String entryName() {
        return "Tags";
    }

    @Override
    protected String entryDescription() {
        return "Registry element lists used for mod functionality.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(MineraculousItems.CHEESE.get(AgeingCheese.Age.FRESH));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
