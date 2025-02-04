package dev.thomasglasser.mineraculous.data.modonomicons.wiki.miraculous;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.data.modonomicons.wiki.WikiBookSubProvider;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;

public class CustomizationEntry extends IndexModeEntryProvider {
    private static final String ID = "customization";

    public CustomizationEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("name", () -> BookImagePageModel.create()
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/customization/name.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Name");
        pageText("""
                By default, there is no set name for Miraculous users.
                If a name is not set, it will simply obfuscate the player's name.
                You can set a name with the '/miraculous <miraculous> name <name>' command.
                """);

        page("advanced", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Advanced Customization");
        pageText("""
                NOTE: The following features are only available if the server manually enables it.
                The mod can be further customized by putting files in a subfolder of the 'miraculouslooks' folder in the client's minecraft directory.
                """);

        page("suit", () -> BookImagePageModel.create()
                // TODO: Custom suit image
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/customization/suit.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Suit");
        pageText("""
                Miraculous suits can be customized in the 'suits/<namespace>/<miraculous>' subfolder.
                You can set a look with the '/miraculous <miraculous> look suit <id>' command.
                You can provide a custom texture, model, glowmask, and transformation frames with an id that is used in the command.
                The only required file is the texture.
                If only the texture is provided, it will use the default model.
                """);

        page("miraculous", () -> BookImagePageModel.create()
                // TODO: Custom miraculous image
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/customization/miraculous.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Miraculous");
        pageText("""
                Miraculous in hidden mode can be customized in the 'miraculous/<namespace>/<miraculous>' subfolder.
                You can set a look with the '/miraculous <miraculous> look miraculous <id>' command.
                You can provide a custom texture, model, glowmask, and transforms with an id that is used in the command.
                The only required file is the texture.
                If only the texture is provided, it will use the default model.
                """);

        page("kamikotization", () -> BookImagePageModel.create()
                // TODO: Custom kamikotization image
                .withImages(WikiBookSubProvider.wikiTexture("miraculous/customization/kamikotization.png"))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        pageTitle("Kamikotization");
        pageText("""
                Kamikotization looks can be customized in the 'kamikotizations' subfolder.
                You can provide a custom texture, model, and glowmask with the same name as the kamikotization and it will automatically override the default.
                The only required file is the texture.
                If only the texture is provided, it will use the default model.
                """);
    }

    @Override
    protected String entryName() {
        return "Customization";
    }

    @Override
    protected String entryDescription() {
        return "Per-player miraculous customization";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), MineraculousMiraculous.LADYBUG));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
