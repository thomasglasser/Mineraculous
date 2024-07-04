package dev.thomasglasser.mineraculous.client.gui;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.client.gui.Gui;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousHeartTypes {
    public static final EnumProxy<Gui.HeartType> CATACLYSMED = new EnumProxy<>(Gui.HeartType.class,
            Mineraculous.modLoc("hud/heart/cataclysmed_full"),
            Mineraculous.modLoc("hud/heart/cataclysmed_full_blinking"),
            Mineraculous.modLoc("hud/heart/cataclysmed_half"),
            Mineraculous.modLoc("hud/heart/cataclysmed_half_blinking"),
            Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_full"),
            Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_full_blinking"),
            Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_half"),
            Mineraculous.modLoc("hud/heart/cataclysmed_hardcore_half_blinking"));
}
