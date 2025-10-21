package dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization;

import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.impl.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetItemKamikotizingPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStartKamikotizationTransformationPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ReceiverKamikotizationChatScreen extends AbstractKamikotizationChatScreen {
    public static final Component ACCEPT = Component.translatable("gui.kamikotization.chat.accept");

    private final UUID other;
    private final KamikotizationData kamikotizationData;
    private final SlotInfo slotInfo;

    protected Button accept;

    public ReceiverKamikotizationChatScreen(UUID other, KamikotizationData kamikotizationData, SlotInfo slotInfo) {
        super("", kamikotizationData.kamikoData().faceMaskTexture());
        this.other = other;
        this.kamikotizationData = kamikotizationData;
        this.slotInfo = slotInfo;
    }

    @Override
    protected void init() {
        super.init();
        this.accept = Button.builder(ACCEPT, button -> onClose(false, true))
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
                .build();
        this.addRenderableWidget(this.accept);
    }

    @Override
    public void onClose(boolean cancel, boolean initiated) {
        if (cancel) {
            if (initiated) {
                if (!MineraculousServerConfig.get().enableKamikotizationRejection.get()) {
                    return;
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, true));
            }
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetItemKamikotizingPayload(Optional.empty(), false, slotInfo));
        } else {
            TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationTransformationPayload(kamikotizationData, slotInfo));
            TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(other, minecraft.player.getUUID(), kamikotizationData.kamikotization().getKey()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
        }
        finalizeClose();
    }
}
