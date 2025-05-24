package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundKamikotizationTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ReceiverKamikotizationChatScreen extends AbstractKamikotizationChatScreen {
    private final UUID other;
    private final KamikotizationData kamikotizationData;

    protected Button accept;

    public ReceiverKamikotizationChatScreen(UUID other, KamikotizationData kamikotizationData) {
        super("", kamikotizationData.kamikoData().faceMaskTexture());
        this.other = other;
        this.kamikotizationData = kamikotizationData;
    }

    @Override
    protected void init() {
        super.init();
        this.accept = Button.builder(Component.translatable(ACCEPT), button -> onClose(false, true))
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
                .build();
        this.addRenderableWidget(this.accept);
    }

    @Override
    public void onClose(boolean cancel, boolean initiated) {
        if (cancel) {
            if (initiated) {
                if (!MineraculousServerConfig.get().enableKamikotizationRejection.get())
                    return;
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, true));
            }
        } else {
            if (this.minecraft.player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).containsKey(kamikotizationData.kamikotization()))
                TommyLibServices.NETWORK.sendToServer(new ServerboundSyncKamikotizationLookPayload(FlattenedKamikotizationLookData.flatten(kamikotizationData.kamikotization())));
            TommyLibServices.NETWORK.sendToServer(new ServerboundKamikotizationTransformPayload(kamikotizationData, true, false, false, minecraft.player.position().add(0, 1, 0)));
            TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(other, minecraft.player.getUUID(), kamikotizationData.kamikotization()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
        }
        finalizeClose();
    }
}
