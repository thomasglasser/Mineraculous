package dev.thomasglasser.mineraculous.client.gui.screens;

import org.jetbrains.annotations.Nullable;

public interface RadialMenuOption {
    String translationKey();

    default @Nullable Integer colorOverride() {
        return null;
    }
}
