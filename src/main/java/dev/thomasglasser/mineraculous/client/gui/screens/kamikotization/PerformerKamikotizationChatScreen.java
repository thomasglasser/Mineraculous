package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetToggleTagPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class PerformerKamikotizationChatScreen extends AbstractKamikotizationChatScreen {
    private final UUID other;
    private final BlockPos otherPos;

    public PerformerKamikotizationChatScreen(String performerName, String targetName, UUID other, BlockPos otherPos) {
        super(performerName.isEmpty() ? Component.translatable(INTRO_NAMELESS, targetName).getString() : Component.translatable(INTRO_NAME, targetName, performerName).getString());
        this.other = other;
        this.otherPos = otherPos;
    }

    public PerformerKamikotizationChatScreen(String performerName, String targetName, Player other) {
        this(performerName, targetName, other.getUUID(), other.blockPosition());
    }

    @Override
    public void onClose(boolean cancel, boolean initiated) {
        if (cancel) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(this.minecraft.player.getUUID(), otherPos.above()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
            if (initiated)
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, true));
        } else {
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, false));
        }
        close();
    }
}
