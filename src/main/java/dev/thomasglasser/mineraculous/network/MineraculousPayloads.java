package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import dev.thomasglasser.tommylib.api.network.PayloadInfo;
import java.util.List;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MineraculousPayloads {
    public static List<PayloadInfo<?>> PAYLOADS = List.<PayloadInfo<?>>of(
            // Serverbound
            new PayloadInfo<>(ServerboundSetDeltaMovementPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetDeltaMovementPayload.CODEC),
            new PayloadInfo<>(ServerboundWalkMidSwingingPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundWalkMidSwingingPayload.CODEC),
            new PayloadInfo<>(ServerboundJumpMidSwingingPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundJumpMidSwingingPayload.CODEC),
            new PayloadInfo<>(ServerboundMiraculousTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundMiraculousTransformPayload.CODEC),
            new PayloadInfo<>(ServerboundRequestMiraculousDataSetSyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestMiraculousDataSetSyncPayload.CODEC),
            new PayloadInfo<>(ServerboundSetMiraculousPowerActivatedPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetMiraculousPowerActivatedPayload.CODEC),
            new PayloadInfo<>(ServerboundActivateToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundActivateToolPayload.CODEC),
            new PayloadInfo<>(ServerboundRequestInventorySyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestInventorySyncPayload.CODEC),
            new PayloadInfo<>(ServerboundStealItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealItemPayload.CODEC),
            new PayloadInfo<>(ServerboundStealCuriosPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealCuriosPayload.CODEC),
            new PayloadInfo<>(ServerboundWakeUpPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundWakeUpPayload.CODEC),
            new PayloadInfo<>(ServerboundTryBreakItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTryBreakItemPayload.CODEC),
            new PayloadInfo<>(ServerboundSetLadybugYoyoAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetLadybugYoyoAbilityPayload.CODEC),
            new PayloadInfo<>(ServerboundSetCatStaffAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetCatStaffAbilityPayload.CODEC),
            new PayloadInfo<>(ServerboundSetButterflyCaneAbilityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetButterflyCaneAbilityPayload.CODEC),
            new PayloadInfo<>(ServerboundSetPlayerAttackTargetPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetPlayerAttackTargetPayload.CODEC),
            new PayloadInfo<>(ServerboundSetToggleTagPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetToggleTagPayload.CODEC),
            new PayloadInfo<>(ServerboundSpawnTamedKamikoPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSpawnTamedKamikoPayload.CODEC),
            new PayloadInfo<>(ServerboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenVictimKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenPerformerKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundCloseKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundKamikotizationTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundKamikotizationTransformPayload.CODEC),
            new PayloadInfo<>(ServerboundPutToolInHandPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundPutToolInHandPayload.CODEC),
            new PayloadInfo<>(ServerboundEquipToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundEquipToolPayload.CODEC),
            new PayloadInfo<>(ServerboundHurtEntityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundHurtEntityPayload.CODEC),
            new PayloadInfo<>(ServerboundTriggerKamikotizationAdvancementsPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTriggerKamikotizationAdvancementsPayload.CODEC),
            new PayloadInfo<>(ServerboundSyncCustomizationPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSyncCustomizationPayload.CODEC),
            new PayloadInfo<>(ServerboundSyncKamikotizationLooksPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSyncKamikotizationLooksPayload.CODEC),
            new PayloadInfo<>(ServerboundSyncKamikotizationLookPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSyncKamikotizationLookPayload.CODEC),
            new PayloadInfo<>(ServerboundTameEntityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTameEntityPayload.CODEC),
            new PayloadInfo<>(ServerboundRenounceMiraculousPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRenounceMiraculousPayload.CODEC),
            new PayloadInfo<>(ServerboundSetKamikotizationPowerActivatedPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetKamikotizationPowerActivatedPayload.CODEC),
            new PayloadInfo<>(ServerboundChangeVipDataPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundChangeVipDataPayload.CODEC),
            new PayloadInfo<>(ServerboundTransferMiraculousPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTransferMiraculousPayload.CODEC),
            new PayloadInfo<>(ServerboundBeginLuckyCharmWorldRecoveryPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundBeginLuckyCharmWorldRecoveryPayload.CODEC),
            new PayloadInfo<>(ServerboundSetOwnerPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetOwnerPayload.CODEC),
            new PayloadInfo<>(ServerboundSendEmptyLeftClickPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSendEmptyLeftClickPayload.CODEC),
            new PayloadInfo<>(ServerboundSendOffhandSwingPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSendOffhandSwingPayload.CODEC),

            // Clientbound
            new PayloadInfo<>(ClientboundSetPlayerRotationPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSetPlayerRotationPayload.CODEC),
            new PayloadInfo<>(ClientboundPlayerAnimationPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundPlayerAnimationPayload.CODEC),
            new PayloadInfo<>(ClientboundCatStaffPerchPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundCatStaffPerchPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncMiraculousDataSetPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncMiraculousDataSetPayload.CODEC),
            new PayloadInfo<>(ClientboundToggleNightVisionPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundToggleNightVisionPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncInventoryPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncInventoryPayload.CODEC),
            new PayloadInfo<>(ClientboundSetCameraEntityPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSetCameraEntityPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenKamikotizationSelectionScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenKamikotizationSelectionScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenVictimKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenPerformerKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundCloseKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncKamikotizationDataPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncKamikotizationDataPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncLadybugYoyoPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncLadybugYoyoPayload.CODEC),
            new PayloadInfo<>(ClientboundRequestSyncSuitLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRequestSyncSuitLookPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncSuitLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncSuitLookPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncMiraculousLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncMiraculousLookPayload.CODEC),
            new PayloadInfo<>(ClientboundRequestSyncMiraculousLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRequestSyncMiraculousLookPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncKamikotizationLooksPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncKamikotizationLooksPayload.CODEC),
            new PayloadInfo<>(ClientboundRequestSyncKamikotizationLooksPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRequestSyncKamikotizationLooksPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncKamikotizationLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncKamikotizationLookPayload.CODEC),
            new PayloadInfo<>(ClientboundRequestSyncKamikotizationLookPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRequestSyncKamikotizationLookPayload.CODEC),
            new PayloadInfo<>(ClientboundChangeVipDataPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundChangeVipDataPayload.CODEC),
            new PayloadInfo<>(ClientboundRefreshVipDataPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRefreshVipDataPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenMiraculousTransferScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenMiraculousTransferScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundSendRightHandParticlesPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSendRightHandParticlesPayload.CODEC),
            new PayloadInfo<>(ClientboundCheckLuckyCharmWorldRecoveryPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundCheckLuckyCharmWorldRecoveryPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenLookCustomizationScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenLookCustomizationScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncArrowPickupStackPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncArrowPickupStackPayload.CODEC));

    public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
        PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));
    }
}
