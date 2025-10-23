package dev.thomasglasser.mineraculous.impl.network;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTriggerData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import dev.thomasglasser.tommylib.api.network.PayloadInfo;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MineraculousPayloads {
    public static List<PayloadInfo<?>> PAYLOADS = ImmutableList.of(
            // Serverbound
            new PayloadInfo<>(ServerboundUpdateStaffPerchLength.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundUpdateStaffPerchLength.CODEC),
            new PayloadInfo<>(ServerboundUpdateStaffInputPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundUpdateStaffInputPayload.CODEC),
            new PayloadInfo<>(ServerboundUpdateYoyoInputPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundUpdateYoyoInputPayload.CODEC),
            new PayloadInfo<>(ServerboundMiraculousTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundMiraculousTransformPayload.CODEC),
            new PayloadInfo<>(ServerboundRequestInventorySyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestInventorySyncPayload.CODEC),
            new PayloadInfo<>(ServerboundStealItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealItemPayload.CODEC),
            new PayloadInfo<>(ServerboundStealCurioPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealCurioPayload.CODEC),
            new PayloadInfo<>(ServerboundWakeUpPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundWakeUpPayload.CODEC),
            new PayloadInfo<>(ServerboundTryBreakItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTryBreakItemPayload.CODEC),
            new PayloadInfo<>(ServerboundSetPlayerAttackTargetPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetPlayerAttackTargetPayload.CODEC),
            new PayloadInfo<>(ServerboundSpawnTamedKamikoPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSpawnTamedKamikoPayload.CODEC),
            new PayloadInfo<>(ServerboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenVictimKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundOpenPerformerKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundCloseKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ServerboundStartKamikotizationTransformationPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStartKamikotizationTransformationPayload.CODEC),
            new PayloadInfo<>(ServerboundPutMiraculousToolInHandPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundPutMiraculousToolInHandPayload.CODEC),
            new PayloadInfo<>(ServerboundPutKamikotizationToolInHandPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundPutKamikotizationToolInHandPayload.CODEC),
            new PayloadInfo<>(ServerboundEquipToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundEquipToolPayload.CODEC),
            new PayloadInfo<>(ServerboundTriggerKamikotizationAdvancementsPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTriggerKamikotizationAdvancementsPayload.CODEC),
            new PayloadInfo<>(ServerboundRenounceMiraculousPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRenounceMiraculousPayload.CODEC),
            new PayloadInfo<>(ServerboundUpdateSpecialPlayerDataPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundUpdateSpecialPlayerDataPayload.CODEC),
            new PayloadInfo<>(ServerboundTransferMiraculousPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundTransferMiraculousPayload.CODEC),
            new PayloadInfo<>(ServerboundRevertConvertedEntityPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRevertConvertedEntityPayload.CODEC),
            new PayloadInfo<>(ServerboundToggleActivePayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundToggleActivePayload.CODEC),
            new PayloadInfo<>(ServerboundSetRadialMenuProviderOptionPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetRadialMenuProviderOptionPayload.CODEC),
            new PayloadInfo<>(ServerboundUpdateYoyoLengthPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundUpdateYoyoLengthPayload.CODEC),
            new PayloadInfo<>(ServerboundSetFaceMaskTexturePayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetFaceMaskTexturePayload.CODEC),
            new PayloadInfo<>(ServerboundSetSpectationInterruptedPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetSpectationInterruptedPayload.CODEC),
            new PayloadInfo<>(ServerboundEmptyLeftClickItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundEmptyLeftClickItemPayload.CODEC),
            new PayloadInfo<>(ServerboundStartKamikotizationDetransformationPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStartKamikotizationDetransformationPayload.CODEC),
            new PayloadInfo<>(ServerboundRemoteDamagePayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRemoteDamagePayload.CODEC),
            new PayloadInfo<>(ServerboundHandleEntityRemovedOnClientPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundHandleEntityRemovedOnClientPayload.CODEC),
            new PayloadInfo<>(ServerboundToggleNightVisionPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundToggleNightVisionPayload.CODEC),
            new PayloadInfo<>(ServerboundActivatePowerPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundActivatePowerPayload.CODEC),
            new PayloadInfo<>(ServerboundToggleBuffsPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundToggleBuffsPayload.CODEC),
            new PayloadInfo<>(ServerboundSetItemKamikotizingPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundSetItemKamikotizingPayload.CODEC),

            // Clientbound
            new PayloadInfo<>(ClientboundCalculateYoyoRenderLengthPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundCalculateYoyoRenderLengthPayload.CODEC),
            new PayloadInfo<>(ClientboundToggleNightVisionShaderPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundToggleNightVisionShaderPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncInventoryPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncInventoryPayload.CODEC),
            new PayloadInfo<>(ClientboundSetCameraEntityPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSetCameraEntityPayload.CODEC),
            new PayloadInfo<>(ClientboundBeginKamikotizationSelectionPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundBeginKamikotizationSelectionPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenVictimKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenVictimKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenPerformerKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenPerformerKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundRemoteCloseKamikotizationChatScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundRemoteCloseKamikotizationChatScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundUpdateSpecialPlayerDataPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundUpdateSpecialPlayerDataPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncSpecialPlayerChoicesPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncSpecialPlayerChoicesPayload.CODEC),
            new PayloadInfo<>(ClientboundOpenMiraculousTransferScreenPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundOpenMiraculousTransferScreenPayload.CODEC),
            new PayloadInfo<>(ClientboundAddRightHandParticlesPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundAddRightHandParticlesPayload.CODEC),
            new PayloadInfo<>(ClientboundSyncArrowPickupStackPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncArrowPickupStackPayload.CODEC));

    public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MineraculousConstants.MOD_ID);
        PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));

        // Attachment Syncing Registration
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.ABILITY_EFFECTS.get(), AbilityEffectData.STREAM_CODEC);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO.get(), ThrownLadybugYoyoData.STREAM_CODEC);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.MIRACULOUSES.get(), MiraculousesData.STREAM_CODEC);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET.get(), MiraculousLadybugTargetData.STREAM_CODEC);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER.get(), ByteBufCodecs.optional(MiraculousLadybugTriggerData.STREAM_CODEC));
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.KAMIKOTIZATION.get(), ByteBufCodecs.optional(KamikotizationData.STREAM_CODEC));
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE.get(), ByteBufCodecs.BOOL);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO.get(), ByteBufCodecs.optional(LeashingLadybugYoyoData.STREAM_CODEC));
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.PERCHING_CAT_STAFF.get(), PerchingCatStaffData.STREAM_CODEC);
        NeoForgeNetworkUtils.registerSyncedAttachment(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF.get(), TravelingCatStaffData.STREAM_CODEC);
    }
}
