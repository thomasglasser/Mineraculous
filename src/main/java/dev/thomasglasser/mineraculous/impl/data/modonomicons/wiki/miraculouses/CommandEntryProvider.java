package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.miraculouses;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import net.minecraft.world.item.Items;

public class CommandEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "command";

    public CommandEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("charged", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "charged");
        add(context().pageText(), """
                The "charged" subcommand allows you to set the charged state of a kwami.
                It requires the commands enabled permission level.
                The kwami must be present in the world to change its charged state.
                """);

        page("power_level", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "power_level");
        add(context().pageText(), """
                The "power_level" subcommand allows you to set the power level of a miraculous.
                It requires the commands enabled permission level.
                It has a maximum value of 100.
                """);
    }

    @Override
    protected String entryName() {
        return "Command";
    }

    @Override
    protected String entryDescription() {
        return "The /miraculous command can be executed to customize your Miraculous.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.COMMAND_BLOCK);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
