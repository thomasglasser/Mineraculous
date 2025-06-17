package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.support;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.WikiBookSubProvider;
import net.minecraft.world.item.Items;

public class ContactEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "contact";

    public ContactEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("github", () -> BookImagePageModel.create()
                .withAnchor("github")
                .withImages(ModPagesEntryProvider.GITHUB_LOCATION))
                        .withTitle(context().pageTitle())
                        .withText(context().pageText());

        add(context().pageTitle(), "GitHub");
        add(context().pageText(), """
                For issues and feature requests,
                [GitHub](https://github.com/thomasglasser/Mineraculous/issues) is the best place to go.
                """);

        page("discord", () -> BookImagePageModel.create()
                .withAnchor("discord")
                .withImages(WikiBookSubProvider.wikiTexture("support/contact/discord.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Discord");
        add(context().pageText(), """
                For general questions and discussions,
                the [Discord](https://discord.gg/Vd6yX2ngWX) is the best place to go.
                """);

        page("email", () -> BookTextPageModel.create()
                .withAnchor("email")
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Email");
        add(context().pageText(), """
                For business inquiries and other matters,
                you can reach out to at [mineraculous@thomasglasser.dev](mailto:mineraculous@thomasglasser.dev).
                """);
    }

    @Override
    protected String entryName() {
        return "Contact";
    }

    @Override
    protected String entryDescription() {
        return "Locations to contact the mod and its developers.";
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
