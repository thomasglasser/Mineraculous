package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.PayloadInfo;
import java.util.ArrayList;
import java.util.List;

public class MineraculousPayloads {
    public static List<PayloadInfo<?>> PAYLOADS = new ArrayList<>();

    public static void init() {
        // Serverbound
        PAYLOADS.add(new PayloadInfo<>(ServerboundMiraculousTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundMiraculousTransformPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundRequestMiraculousDataSetSyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestMiraculousDataSetSyncPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetPowerActivatedPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetPowerActivatedPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundActivateToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundActivateToolPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundRequestInventorySyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestInventorySyncPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundStealItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealItemPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundStealCuriosPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealCuriosPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundWakeUpPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundWakeUpPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundTryBreakItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTryBreakItemPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetLadybugYoyoAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetLadybugYoyoAbilityPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetCatStaffAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetCatStaffAbilityPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetButterflyCaneAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetButterflyCaneAbilityPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetPlayerAttackTargetPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetPlayerAttackTargetPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSetToggleTagPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetToggleTagPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundSpawnTamedKamikoPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSpawnTamedKamikoPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenVictimKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenPerformerKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundCloseKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundKamikotizationTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundKamikotizationTransformPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundPutToolInHandPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundPutToolInHandPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundEquipToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundEquipToolPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ServerboundHurtEntityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundHurtEntityPayload.CODEC));

        // Clientbound
        PAYLOADS.add(new PayloadInfo<>(ClientboundSyncCurioPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncCurioPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundSyncMiraculousDataSetPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncMiraculousDataSetPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundToggleNightVisionPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundToggleNightVisionPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundSyncInventoryPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncInventoryPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundSetCameraEntityPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSetCameraEntityPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundOpenKamikotizationSelectionScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenKamikotizationSelectionScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenVictimKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenPerformerKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundCloseKamikotizationChatScreenPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundSyncKamikotizationDataPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncKamikotizationDataPayload.CODEC));
        PAYLOADS.add(new PayloadInfo<>(ClientboundSyncLadybugYoyoPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncLadybugYoyoPayload.CODEC));
    }
}
