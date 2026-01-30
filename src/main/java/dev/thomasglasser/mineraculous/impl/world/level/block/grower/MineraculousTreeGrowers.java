package dev.thomasglasser.mineraculous.impl.world.level.block.grower;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.data.worldgen.features.MineraculousTreeFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class MineraculousTreeGrowers {
    public static final TreeGrower ALMOND = new TreeGrower(MineraculousConstants.modLoc("almond").toString(), 0.1F,
            Optional.empty(),
            Optional.empty(),
            Optional.of(MineraculousTreeFeatures.ALMOND),
            Optional.of(MineraculousTreeFeatures.FANCY_ALMOND),
            Optional.of(MineraculousTreeFeatures.ALMOND),
            Optional.of(MineraculousTreeFeatures.ALMOND));
}
