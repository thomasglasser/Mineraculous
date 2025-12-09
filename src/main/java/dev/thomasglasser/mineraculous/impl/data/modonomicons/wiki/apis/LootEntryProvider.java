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

public class LootEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "loot";

    public LootEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("functions", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Functions");
        add(context().pageText(), """
                There is one loot function added by the mod:
                - "mineraculous:dye_randomly": Dyes the resulting items random colors.
                """);

        page("conditions", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Conditions");
        add(context().pageText(), """
                There is one loot condition added by the mod:
                - "mineraculous:has_item": Checks if the relevant entity has the given item in its inventory.
                """);

        page("number_providers", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Number Providers");
        add(context().pageText(), """
                There is one number provider added by the mod:
                - "mineraculous:power_level_multiplier": Multiplies the given number by the entity's Miraculous or Kamikotization power level.
                """);
    }

    @Override
    protected String entryName() {
        return "Loot";
    }

    @Override
    protected String entryDescription() {
        return "Extensions of the loot table system.";
    }

    @Override
    protected BookIconModel entryIcon() {
        ItemStack stack = Items.APPLE.getDefaultInstance();
        stack.set(MineraculousDataComponents.LUCKY_CHARM, new LuckyCharm(Optional.empty(), Util.NIL_UUID, 0));
        return BookIconModel.create(stack);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
