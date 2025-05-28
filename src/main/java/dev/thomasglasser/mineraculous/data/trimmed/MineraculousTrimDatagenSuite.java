package dev.thomasglasser.mineraculous.data.trimmed;

import dev.dhyces.trimmed.api.data.TrimDatagenSuite;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armortrim.MineraculousTrimPatterns;
import java.util.function.BiConsumer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MineraculousTrimDatagenSuite extends TrimDatagenSuite {
    public MineraculousTrimDatagenSuite(GatherDataEvent event, BiConsumer<String, String> lang) {
        super(event, Mineraculous.MOD_ID, lang);
    }

    @Override
    public void generate() {
        makePattern(MineraculousTrimPatterns.LADYBUG, MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE.get(), false, patternConfig -> patternConfig
                .createCopyRecipe(Items.RED_CONCRETE));
        makePattern(MineraculousTrimPatterns.CAT, MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE.get(), false, patternConfig -> patternConfig
                .createCopyRecipe(Items.LIME_CONCRETE));
        makePattern(MineraculousTrimPatterns.BUTTERFLY, MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE.get(), false, patternConfig -> patternConfig
                .createCopyRecipe(Items.PURPLE_CONCRETE));
    }
}
