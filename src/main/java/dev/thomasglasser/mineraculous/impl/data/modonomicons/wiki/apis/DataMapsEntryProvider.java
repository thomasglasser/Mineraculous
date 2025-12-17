package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DataMapsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "data_maps";

    public DataMapsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("lucky_charms", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Lucky Charms");
        add(context().pageText(), """
                Contextual lucky charms are determined for kamikotizations, miraculous holders, and entities via data maps.
                They are located in "data/mineraculous/kamikotization/lucky_charms.json",
                "data/mineraculous/miraculous/lucky_charms.json",
                and "data/mineraculous/entity_type/lucky_charms.json".
                Generators for these can be found online [here](https://beta-jsons.thomasglasser.dev/partners/).
                Generators for the loot table can be found [here](https://beta-jsons.thomasglasser.dev/loot-table/).
                (Note: At this time, to generate a lucky charm loot table, you must use a preset to set the "type" field to "mineraculous:lucky_charm".
                Searching the presets for "lucky_charm" will yield valid results.)
                """);

        page("miraculous_buffs", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Miraculous Buffs");
        add(context().pageText(), """
                Miraculous buffs are provided to miraculous holders and kamikotized entities while transformed.
                These are determined via two data maps:
                - "data/mineraculous/mob_effect/miraculous_effects.json" for mob effects ([Generator](https://beta-jsons.thomasglasser.dev/mineraculous/data-map-miraculous-effects/))
                - "data/mineraculous/attribute/miraculous_attribute_modifiers.json" for attributes ([Generator](https://beta-jsons.thomasglasser.dev/mineraculous/data-map-miraculous-attribute-modifiers/))
                """);

        page("ageables", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Ageables");
        add(context().pageText(), """
                There is an ageable data map for blocks implementing the AgeingCheese interface that determines the block to change to when ageing.
                This is located at "data/mineraculous/block/ageables.json".
                A generator for this can be found online [here](https://beta-jsons.thomasglasser.dev/mineraculous/data-map-ageables/).
                """);
    }

    @Override
    protected String entryName() {
        return "Data Maps";
    }

    @Override
    protected String entryDescription() {
        return "Data-driven JSON maps used to expand mod functionality";
    }

    @Override
    protected BookIconModel entryIcon() {
        ItemStack stack = new ItemStack(Items.BOW);
        stack.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.empty(), Util.NIL_UUID, 0));
        return BookIconModel.create(stack);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
