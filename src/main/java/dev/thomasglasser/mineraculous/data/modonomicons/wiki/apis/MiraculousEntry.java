package dev.thomasglasser.mineraculous.data.modonomicons.wiki.apis;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.IndexModeEntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;

public class MiraculousEntry extends IndexModeEntryProvider {
    private static final String ID = "miraculous";

    public MiraculousEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        page("generator", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Generator");
        add(context().pageText(), """
                A generator for miraculous can be found online [here](https://jsons.thomasglasser.dev/mineraculous/miraculous/).
                """);

        page("fields", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Fields");
        add(context().pageText(), """
                Miraculous have a few fields that determine how they work:
                - acceptable_slot: The curios slot that the miraculous can be placed in.
                - active_ability: The ability that is activated when the Activate Power button (default: O) is pressed.
                - color: The color of the miraculous.
                - passive_abilities: The abilities that are active at all times when transformed.
                - tool: The tool that is given to the miraculous holder.
                - tool_slot: The slot that the tool is put in when transformed.
                - transform_sound: The sound that is played when transformed.
                - transformation_frames: The number of frames that the miraculous transforms.
                """);

        page("example", () -> BookTextPageModel.create()
                .withTitle(context().pageTitle())
                .withText(context().pageText()));

        add(context().pageTitle(), "Example");
        add(context().pageText(), """
                The following is an example of a ladybug miraculous:
                {
                  "acceptable_slot": "earrings",
                  "active_ability": "mineraculous:lucky_charm",
                  "color": "#DD1731",
                  "passive_abilities": [
                    "mineraculous:miraculous_ladybug"
                  ],
                  "tool": {
                    "id": "mineraculous:ladybug_yoyo"
                  },
                  "tool_slot": "belt",
                  "transform_sound": "mineraculous:ladybug_transform",
                  "transformation_frames": 9
                }
                """);
    }

    @Override
    protected String entryName() {
        return "Miraculous";
    }

    @Override
    protected String entryDescription() {
        return "Jewels that provide special abilities and fancy a super suit to the holder";
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Miraculous.createMiraculousStack(MineraculousMiraculous.CAT));
    }

    @Override
    protected String entryId() {
        return ID;
    }
}
