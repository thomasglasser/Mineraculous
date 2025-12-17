package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.dependencies;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import net.minecraft.world.item.Items;

public class VoiceChatEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "voice_chat";

    public VoiceChatEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("private_chat", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Private Chat");
        add(context().pageText(), """
                Private chat applies to both text and voice chat when Simple Voice Chat is installed,
                allowing players to communicate from anywhere without anyone else hearing them.
                """);
    }

    @Override
    protected String entryName() {
        return "Simple Voice Chat";
    }

    @Override
    protected String entryDescription() {
        return "Simple Voice Chat is a voice and proximity chat mod.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.GOAT_HORN);
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
