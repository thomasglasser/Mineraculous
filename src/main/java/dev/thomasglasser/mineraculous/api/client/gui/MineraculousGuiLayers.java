package dev.thomasglasser.mineraculous.api.client.gui;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousGuiLayers {
    public static final ResourceLocation STEALING_PROGRESS_BAR = Mineraculous.modLoc("stealing_progress_bar");
    public static final ResourceLocation REVOKE_BUTTON = Mineraculous.modLoc("revoke_button");
    public static final ResourceLocation KAMIKO_HOTBAR = Mineraculous.modLoc("kamiko_hotbar");
    public static final ResourceLocation KAMIKO_TOOLTIP = Mineraculous.modLoc("kamiko_tooltip");

    private static final Set<ResourceLocation> ALLOWED_SPECTATING_GUI_LAYERS = ReferenceOpenHashSet.of(
            MineraculousGuiLayers.REVOKE_BUTTON,
            MineraculousGuiLayers.KAMIKO_HOTBAR,
            MineraculousGuiLayers.KAMIKO_TOOLTIP,
            VanillaGuiLayers.DEMO_OVERLAY,
            VanillaGuiLayers.DEBUG_OVERLAY,
            VanillaGuiLayers.SCOREBOARD_SIDEBAR,
            VanillaGuiLayers.OVERLAY_MESSAGE,
            VanillaGuiLayers.TITLE,
            VanillaGuiLayers.CHAT,
            VanillaGuiLayers.TAB_LIST,
            VanillaGuiLayers.SUBTITLE_OVERLAY,
            VanillaGuiLayers.SAVING_INDICATOR);

    public static void addAllowedSpectatingGuiLayer(ResourceLocation layer) {
        ALLOWED_SPECTATING_GUI_LAYERS.add(layer);
    }

    @ApiStatus.Internal
    public static boolean isAllowedSpectatingGuiLayer(ResourceLocation layer) {
        return ALLOWED_SPECTATING_GUI_LAYERS.contains(layer);
    }
}
