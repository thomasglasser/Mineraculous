package dev.thomasglasser.mineraculous.api.client.gui;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousGuiLayers {
    /// Renders when a player is stealing from another player.
    public static final ResourceLocation STEALING_PROGRESS_BAR = MineraculousConstants.modLoc("stealing_progress_bar");
    /// Renders when {@link dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.SyncedTransientAbilityEffectData#allowKamikotizationRevocation()} is true.
    public static final ResourceLocation REVOKE_BUTTON = MineraculousConstants.modLoc("revoke_button");
    /// Renders when in the Kamiko GUI with the {@link dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu} active.
    public static final ResourceLocation KAMIKO_HOTBAR = MineraculousConstants.modLoc("kamiko_hotbar");
    /// Renders when in the Kamiko GUI with a {@link dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem} is selected.
    public static final ResourceLocation KAMIKO_TOOLTIP = MineraculousConstants.modLoc("kamiko_tooltip");

    private static final Set<ResourceLocation> ALLOWED_SPECTATING_GUI_LAYERS = ObjectOpenHashSet.of(
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

    /// Allows the provided GUI layer to be visible when {@link dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.SyncedTransientAbilityEffectData#spectatingId()} is present.
    public static void addAllowedSpectatingGuiLayer(ResourceLocation layer) {
        ALLOWED_SPECTATING_GUI_LAYERS.add(layer);
    }

    @ApiStatus.Internal
    public static boolean isAllowedSpectatingGuiLayer(ResourceLocation layer) {
        return ALLOWED_SPECTATING_GUI_LAYERS.contains(layer);
    }
}
