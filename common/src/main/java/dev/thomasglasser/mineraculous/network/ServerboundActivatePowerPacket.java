package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ServerboundActivatePowerPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_activate_power");

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		MiraculousData data = Services.DATA.getMiraculousData(player);
		data.miraculous().getOrCreateTag().putInt(MiraculousItem.TAG_REMAININGTICKS, MiraculousItem.FIVE_MINUTES);
		Services.DATA.setMiraculousData(new MiraculousData(data.transformed(), data.miraculous(), data.miraculousData(), data.tool(), data.powerLevel(), true, true), player, true);
		Services.CURIOS.setStackInSlot(player, data.miraculousData(), data.miraculous(), true);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
