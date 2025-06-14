package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStartKamikotizationTransformationPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ReceiverKamikotizationChatScreen extends AbstractKamikotizationChatScreen {
    public static final String ACCEPT = "gui.kamikotization.chat.accept";

    private final UUID other;
    private final KamikotizationData kamikotizationData;
    private final Either<Integer, CuriosData> slotInfo;

    protected Button accept;

    public ReceiverKamikotizationChatScreen(UUID other, KamikotizationData kamikotizationData, Either<Integer, CuriosData> slotInfo) {
        super("", kamikotizationData.kamikoData().faceMaskTexture());
        this.other = other;
        this.kamikotizationData = kamikotizationData;
        this.slotInfo = slotInfo;
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
                if (!MineraculousServerConfig.get().enableKamikotizationRejection.get()) {
                    return;
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, true));
            }
        } else {
            if (this.minecraft.player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION_LOOKS).containsKey(kamikotizationData.kamikotization())) {
                try {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSyncKamikotizationLookPayload(FlattenedKamikotizationLookData.resolve(kamikotizationData.kamikotization())));
                } catch (IOException e) {
                    Mineraculous.LOGGER.error("Failed to resolve kamikotization look for {}", kamikotizationData.kamikotization(), e);
                }
            }
            TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationTransformationPayload(kamikotizationData, slotInfo));
            TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(other, minecraft.player.getUUID(), kamikotizationData.kamikotization().getKey()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
        }
        finalizeClose();
    }
}
