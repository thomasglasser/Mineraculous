package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MineraculousPackets
{
	public static Map<Class<? extends CustomPacket>, Pair<ResourceLocation, CustomPacket.Direction>> PACKETS = new HashMap<>();

	public static void init()
	{
		// Serverbound
		PACKETS.put(ServerboundMiraculousTransformPacket.class, Pair.of(ServerboundMiraculousTransformPacket.ID, CustomPacket.Direction.CLIENT_TO_SERVER));

		// Clientbound
		PACKETS.put(ClientboundMiraculousTransformPacket.class, Pair.of(ClientboundMiraculousTransformPacket.ID, CustomPacket.Direction.SERVER_TO_CLIENT));
	}
}
