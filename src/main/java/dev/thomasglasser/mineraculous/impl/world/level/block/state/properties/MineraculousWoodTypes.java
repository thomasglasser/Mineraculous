package dev.thomasglasser.mineraculous.impl.world.level.block.state.properties;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.world.level.block.state.properties.WoodType;

public class MineraculousWoodTypes {
    public static final WoodType ALMOND = registerWoodType(new WoodType(MineraculousConstants.modLoc("almond").toString(), MineraculousBlockSetTypes.ALMOND));

    public static WoodType registerWoodType(WoodType woodType) {
        return WoodType.register(woodType);
    }
}
