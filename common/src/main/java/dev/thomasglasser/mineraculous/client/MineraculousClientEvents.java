package dev.thomasglasser.mineraculous.client;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.client.model.KamikoModel;
import dev.thomasglasser.mineraculous.client.model.geom.MineraculousModelLayers;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MineraculousClientEvents
{
	@SuppressWarnings("unchecked")
	public static Pair<ModelLayerLocation,LayerDefinition>[] getEntityModelLayers() {
		return new Pair[] {
				new Pair<>(MineraculousModelLayers.KAMIKO,KamikoModel.createBodyLayer())
		};
	}

	public static void onEntityJoinLevel(Entity entity)
	{
		TommyLibServices.NETWORK.sendToServer(new ServerboundRequestMiraculousDataSetSyncPayload(entity.getId()));
	}

	public static void openPowerWheel(Player player)
	{
		if (ClientUtils.getMinecraft().screen == null)
		{
			// TODO: Radial menu with all available powers from all active miraculous
			player.sendSystemMessage(Component.literal("Power Wheel Coming Soon"));
		}
	}
}
