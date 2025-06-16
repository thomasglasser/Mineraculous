package dev.thomasglasser.mineraculous.client.gui.screens.kamikotization;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.network.ServerboundCloseKamikotizationChatScreenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStartKamikotizationTransformationPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTriggerKamikotizationAdvancementsPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
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
            TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationTransformationPayload(kamikotizationData, slotInfo));
            TommyLibServices.NETWORK.sendToServer(new ServerboundTriggerKamikotizationAdvancementsPayload(other, minecraft.player.getUUID(), kamikotizationData.kamikotization().getKey()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundCloseKamikotizationChatScreenPayload(other, false));
        }
        finalizeClose();
    }
}
