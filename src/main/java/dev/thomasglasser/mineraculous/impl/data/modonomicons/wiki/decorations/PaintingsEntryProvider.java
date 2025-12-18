package dev.thomasglasser.mineraculous.impl.data.modonomicons.wiki.decorations;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import dev.thomasglasser.mineraculous.impl.world.entity.decoration.MineraculousPaintingVariants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.Items;

public class PaintingsEntryProvider extends IndexModeEntryProvider {
    public static final String ID = "paintings";

    public PaintingsEntryProvider(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("placeable", () -> BookImagePageModel.create()
                .withImages(paintingLoc(MineraculousPaintingVariants.LADYBUG), paintingLoc(MineraculousPaintingVariants.MINI_LADYBUG), paintingLoc(MineraculousPaintingVariants.CAT), paintingLoc(MineraculousPaintingVariants.MINI_CAT), paintingLoc(MineraculousPaintingVariants.BUTTERFLY), paintingLoc(MineraculousPaintingVariants.MINI_BUTTERFLY))
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Placeable");
        add(context().pageText(), """
                The mod adds a variety of placeable paintings that can be used to brighten up your world.
                """);
    }

    @Override
    protected String entryName() {
        return "Paintings";
    }

    @Override
    protected String entryDescription() {
        return "Decorative paintings to hang on your walls.";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.PAINTING);
    }

    @Override
    protected String entryId() {
        return ID;
    }

    public static ResourceLocation paintingLoc(ResourceKey<PaintingVariant> painting) {
        return painting.location().withPrefix("textures/painting/").withSuffix(".png");
    }
}
