package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MineraculousClientEvents
{
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

	public static void renderStealingProgressBar(GuiGraphics guiGraphics, float partialTick)
	{
		LocalPlayer player = Minecraft.getInstance().player;
		CompoundTag data = TommyLibServices.ENTITY.getPersistentData(player);
		int width = data.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
		if (player != null && width > 0)
		{
			int x = (guiGraphics.guiWidth() - 18) / 2;
			int y = (guiGraphics.guiHeight() + 12) / 2;
			guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
			guiGraphics.fill(RenderType.guiOverlay(), x, y, (int)(x + (width / 5.0)), y + 5, 0xFFFFFFF | -16777216);
		}
	}
}
