package dev.thomasglasser.mineraculous.impl.client.gui;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.client.gui.Gui;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousHeartTypes {
    public static final EnumProxy<Gui.HeartType> CATACLYSMED = new EnumProxy<>(Gui.HeartType.class,
            MineraculousConstants.modLoc("hud/heart/cataclysmed_full"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_full_blinking"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_half"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_half_blinking"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_hardcore_full"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_hardcore_full_blinking"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_hardcore_half"),
            MineraculousConstants.modLoc("hud/heart/cataclysmed_hardcore_half_blinking"));
}
