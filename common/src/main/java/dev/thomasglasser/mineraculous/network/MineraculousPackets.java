package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MineraculousPackets
{
	public static Map<Function<FriendlyByteBuf, ? extends CustomPacket>, Pair<ResourceLocation, CustomPacket.Direction>> PACKETS = new HashMap<>();

	public static void init()
	{
		// Serverbound
		PACKETS.put(ServerboundMiraculousTransformPacket::new, Pair.of(ServerboundMiraculousTransformPacket.ID, CustomPacket.Direction.CLIENT_TO_SERVER));
		PACKETS.put(ServerboundRequestMiraculousDataSetSyncPacket::new, Pair.of(ServerboundRequestMiraculousDataSetSyncPacket.ID, CustomPacket.Direction.CLIENT_TO_SERVER));
		PACKETS.put(ServerboundActivateMainPowerPacket::new, Pair.of(ServerboundActivateMainPowerPacket.ID, CustomPacket.Direction.CLIENT_TO_SERVER));

		// Clientbound
		PACKETS.put(ClientboundMiraculousTransformPacket::new, Pair.of(ClientboundMiraculousTransformPacket.ID, CustomPacket.Direction.SERVER_TO_CLIENT));
		PACKETS.put(ClientboundSyncCurioPacket::new, Pair.of(ClientboundSyncCurioPacket.ID, CustomPacket.Direction.SERVER_TO_CLIENT));
		PACKETS.put(ClientboundSyncMiraculousDataSetPacket::new, Pair.of(ClientboundSyncMiraculousDataSetPacket.ID, CustomPacket.Direction.SERVER_TO_CLIENT));
		PACKETS.put(ClientboundToggleCatVisionPacket::new, Pair.of(ClientboundToggleCatVisionPacket.ID, CustomPacket.Direction.SERVER_TO_CLIENT));
	}
}
