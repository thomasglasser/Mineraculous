package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.PayloadInfo;

import java.util.ArrayList;
import java.util.List;

public class MineraculousPayloads
{
	public static List<PayloadInfo<?>> PAYLOADS = new ArrayList<>();

	public static void init()
	{
		// Serverbound
		PAYLOADS.add(new PayloadInfo<>(ServerboundMiraculousTransformPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundMiraculousTransformPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundRequestMiraculousDataSetSyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestMiraculousDataSetSyncPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundActivateMainPowerPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundActivateMainPowerPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundActivateToolPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundActivateToolPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundRequestInventorySyncPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundRequestInventorySyncPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundStealItemPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealItemPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundStealCuriosPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundStealCuriosPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ServerboundWakeUpPayload.TYPE, ExtendedPacketPayload.Direction.CLIENT_TO_SERVER, ServerboundWakeUpPayload.CODEC));

		// Clientbound
		PAYLOADS.add(new PayloadInfo<>(ClientboundMiraculousTransformPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundMiraculousTransformPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ClientboundSyncCurioPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncCurioPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ClientboundSyncMiraculousDataSetPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncMiraculousDataSetPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ClientboundToggleCatVisionPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundToggleCatVisionPayload.CODEC));
		PAYLOADS.add(new PayloadInfo<>(ClientboundSyncInventoryPayload.TYPE, ExtendedPacketPayload.Direction.SERVER_TO_CLIENT, ClientboundSyncInventoryPayload.CODEC));
	}
}
