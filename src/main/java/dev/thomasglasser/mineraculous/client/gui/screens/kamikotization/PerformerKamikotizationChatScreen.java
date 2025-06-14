package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PerformerKamikotizationChatScreen extends AbstractKamikotizationChatScreen {
    public static final String INTRO_NAME = "gui.kamikotization.chat.intro.name";
    public static final String INTRO_NAMELESS = "gui.kamikotization.chat.intro.nameless";

    private final UUID other;
    private final BlockPos otherPos;

    public PerformerKamikotizationChatScreen(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, UUID other, BlockPos otherPos) {
        super(performerName.isEmpty() ? Component.translatable(INTRO_NAMELESS, targetName).getString() : Component.translatable(INTRO_NAME, targetName, performerName).getString(), faceMaskTexture);
        this.other = other;
        this.otherPos = otherPos;
    }

    public PerformerKamikotizationChatScreen(String performerName, String targetName, Optional<ResourceLocation> faceMaskTexture, Player other) {
        this(performerName, targetName, faceMaskTexture, other.getUUID(), other.blockPosition());
    }

    @Override
    public void onClose(boolean cancel, boolean initiated) {
        if (cancel) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(this.minecraft.player.getUUID(), otherPos.above()));
            if (initiated)
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, true));
        } else {
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
        }
        finalizeClose();
    }
}
