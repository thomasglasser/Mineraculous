package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPacket;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.Entity;

public class MineraculousClientEvents
{
	public static void onEntityJoinLevel(Entity entity)
	{
		TommyLibServices.NETWORK.sendToServer(ServerboundRequestMiraculousDataSetSyncPacket.class, ServerboundRequestMiraculousDataSetSyncPacket.write(entity.getId()));
	}
}
