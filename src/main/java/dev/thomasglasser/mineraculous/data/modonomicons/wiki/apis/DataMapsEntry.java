package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharm;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DataMapsEntry extends IndexModeEntryProvider {
    private static final String ID = "data_maps";

    public DataMapsEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("lucky_charms", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Lucky Charms");
        add(context().pageText(), """
                Contextual lucky charms are determined for entities, miraculous holders, and kamikotizations via data maps.
                They are located in "data/<namespace>/entity_type/lucky_charms.json",
                "data/<namespace>/mineraculous/miraculous/lucky_charms.json",
                and "data/<namespace>/mineraculous/kamikotization/lucky_charms.json".
                Generators can be found online [here](https://snapshot-jsons.thomasglasser.dev/partners/).
                """);
    }

    @Override
    protected String entryName() {
        return "Data Maps";
    }

    @Override
    protected String entryDescription() {
        return "Maps used for mod functionality";
    }

    @Override
    protected BookIconModel entryIcon() {
        ItemStack stack = new ItemStack(Items.BOW);
        stack.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.empty(), 0));
        return BookIconModel.create(stack);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
