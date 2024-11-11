package dev.thomasglasser.mineraculous.client.gui.kamiko;

import java.util.List;
import net.minecraft.network.chat.Component;

public interface KamikoMenuCategory {
    List<KamikoMenuItem> getItems();

    Component getPrompt();
}
