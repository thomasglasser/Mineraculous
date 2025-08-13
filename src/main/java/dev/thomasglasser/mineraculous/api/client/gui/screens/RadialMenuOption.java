package dev.thomasglasser.mineraculous.api.client.gui.screens;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an option in the {@link RadialMenuScreen},
 * defining translation key and optional color override.
 */
public interface RadialMenuOption {
    Component displayName();

    default @Nullable Integer colorOverride() {
        return null;
    }
}
