package dev.thomasglasser.mineraculous.impl.world.level.block.state.properties;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.world.level.block.state.properties.WoodType;

public class MineraculousWoodTypes {
    public static WoodType almondWoodType = registerWoodType(new WoodType(MineraculousConstants.modLoc("almond").toString(), MineraculousBlockSetTypes.almondSetType));

    public static WoodType registerWoodType(WoodType woodType) {
        return WoodType.register(woodType);
    }
}
