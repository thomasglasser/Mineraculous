package dev.thomasglasser.mineraculous.impl.world.level.block.grower;

import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousTreeFeatures;
import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public class MineraculousTreeGrowers {
    public static final TreeGrower ALMOND = new TreeGrower("almond", 0.1F,
            Optional.empty(),
            Optional.empty(),
            Optional.of(MineraculousTreeFeatures.ALMOND),
            Optional.of(MineraculousTreeFeatures.ALMOND),
            Optional.of(MineraculousTreeFeatures.ALMOND),
            Optional.of(MineraculousTreeFeatures.ALMOND));
}
